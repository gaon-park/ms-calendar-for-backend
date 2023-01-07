package com.maple.heroforbackend.service

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.entity.TJwtAuth
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.repository.TJwtAuthRepository
import com.maple.heroforbackend.repository.TUserRepository
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtAuthService(
    private val userDetailsService: UserDetailsService,
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tUserRepository: TUserRepository,
) {
    companion object {
        // todo secret key 프로퍼티화
        private var secretKey = Keys.hmacShaKeyFor("mwMgh7IA1p1N7ldRz7rQKjLB3sen2Z8iRGzDOGpgftg=".toByteArray())

        /**
         * 토큰 유효시간 30분
         */
        private const val tokenValidTime = 30 * 60 * 1000L
    }

    /**
     * JWT 토큰 생성
     */
    @Transactional
    fun createToken(userPk: String, roles: List<String>, response: HttpServletResponse): TJwtAuth {
        // create access token
        val claims = Jwts.claims().setSubject(userPk)
        claims["roles"] = roles
        val now = Date()
        val accessToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenValidTime))
            .signWith(secretKey)
            .compact()

        // refresh token save to db
        val tUser = tUserRepository.findByEmail(userPk) ?: throw BaseException(BaseResponseCode.USER_NOT_FOUND)
        val tJwtAuth =
            TJwtAuth.generate(
                accessToken,
                LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()),
                tUser.email
            )
        tJwtAuthRepository.save(tJwtAuth)

        // refresh token save to httpOnly cookie
        setRefreshTokenToCookie(tJwtAuth, response)

        return tJwtAuth
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    fun reIssue(tJwtAuth: TJwtAuth, response: HttpServletResponse): TJwtAuth {
        val userPk = tJwtAuth.userPk
        var newAuth: TJwtAuth?
        userPk.let {
            newAuth = createToken(it, listOf("ROLE_USER"), response)
            tJwtAuthRepository.delete(tJwtAuth)
        }
        return newAuth ?: throw BaseException(BaseResponseCode.INVALID_AUTH_TOKEN)
    }

    /**
     * JWT 토큰에서 인증 정보 조회
     */
    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUserPk(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    /**
     * 토큰에서 회원 정보 추출
     */
    fun getUserPk(token: String): String =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body.subject

    /**
     * 검증된 데이터 반환
     */
    @Suppress("SwallowedException")
    fun getValidatedAuthData(request: HttpServletRequest, response: HttpServletResponse): TJwtAuth? {
        val access = request.getHeader("X-AUTH-ACCESS-TOKEN") ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        var result: TJwtAuth? = null
        try {
            if (validateAccessToken(access)) {
                result = tJwtAuthRepository.findByAccessKey(access)
            }
        } catch (exception: ExpiredJwtException) {
            val refresh = request.cookies.firstOrNull { it.name == "X-AUTH-REFRESH-TOKEN" }?.value ?: ""
            val tJwtAuthOptional = tJwtAuthRepository.findById(refresh)
            if (tJwtAuthOptional.isPresent) {
                val tJwtAuth = tJwtAuthOptional.get()
                if (tJwtAuth.accessKey == access && !tJwtAuth.expired &&
                    tJwtAuth.expirationDate.isAfter(LocalDateTime.now())
                ) {
                    // reIssue
                    result = reIssue(tJwtAuth, response)
                }
            }
        }

        return result
    }

    /**
     * request 에서 회원 정보 추출
     */
    fun getUserName(request: HttpServletRequest): String = getUserPk(request.getHeader("X-AUTH-ACCESS-TOKEN"))

    /**
     * 토큰의 유효성 + 만료일자 확인
     */
    fun validateAccessToken(jwtToken: String): Boolean {
        val claims = Jwts
            .parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(jwtToken)
        return !claims.body.expiration.before(Date())
    }

    private fun setRefreshTokenToCookie(tJwtAuth: TJwtAuth, response: HttpServletResponse) {
        val cookie = Cookie("X-AUTH-REFRESH-TOKEN", tJwtAuth.id)
        cookie.maxAge = ChronoUnit.SECONDS.between(LocalDateTime.now(), tJwtAuth.expirationDate).toInt()
        cookie.path = "/"
        cookie.isHttpOnly = true
        response.addCookie(cookie)
    }
}
