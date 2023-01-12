package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.MagicVariables.JWT_ACCESS_TOKEN_EXPIRATION_TIME_VALUE
import com.maple.herocalendarforbackend.entity.TJwtAuth
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
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
    }

    /**
     * 최초 로그인 토큰 생성
     */
    @Transactional
    fun firstTokenForLogin(email: String, roles: List<String>, response: HttpServletResponse): String {
        val tUser = tUserRepository.findByEmailAndVerified(email, true) ?: throw BaseException(
            BaseResponseCode.USER_NOT_FOUND
        )
        // 같은 기기(클라이언트)로 로그인 기록이 있다면 삭제 후 재발급
        // 없다면 신규 발급
        tUser.id?.let {
            tJwtAuthRepository.findByUserPk(it)?.let { jwt ->
                return reIssue(jwt, response)
            } ?: kotlin.run {
                return createToken(tUser, roles, response)
            }
        } ?: throw BaseException(BaseResponseCode.DATA_ERROR)
    }


    /**
     * JWT 토큰 생성
     */
    @Transactional
    fun createToken(userPk: TUser, roles: List<String>, response: HttpServletResponse): String {
        // create access token
        val claims = Jwts.claims().setSubject(userPk.email)
        claims["roles"] = roles
        val now = Date()
        val accessToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + JWT_ACCESS_TOKEN_EXPIRATION_TIME_VALUE))
            .signWith(secretKey)
            .compact()

        // refresh token save to db
        val tJwtAuth =
            TJwtAuth.generate(
                LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()),
                userPk
            )
        tJwtAuthRepository.save(tJwtAuth)

        // refresh token save to httpOnly cookie
        setRefreshTokenToCookie(tJwtAuth, response)

        return accessToken
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    fun reIssue(tJwtAuth: TJwtAuth, response: HttpServletResponse): String {
        val userPk = tJwtAuth.userPk
        var newAuth: String?
        userPk.let {
            newAuth = createToken(it, listOf("ROLE_USER"), response)
            tJwtAuthRepository.delete(tJwtAuth)
        }
        return newAuth ?: throw BaseException(BaseResponseCode.INVALID_TOKEN)
    }

    /**
     * 토큰의 유효성 + 만료일자 확인해서 subject 리턴
     */
    fun getUserPkOnValidatedAccessToken(jwtToken: String): String {
        val claims = Jwts
            .parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(jwtToken)
        return if (!claims.body.expiration.before(Date())) claims.body.subject
        else throw BaseException(BaseResponseCode.INVALID_TOKEN)
    }

    /**
     * JWT 토큰에서 인증 정보 조회
     */
    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUserPkOnValidatedAccessToken(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    /**
     * 검증된 데이터 반환
     */
    @Suppress("SwallowedException")
    fun getValidatedAuthData(request: HttpServletRequest): String {
        val bearer = request.getHeader(AUTHORIZATION)
        if (bearer.contains("Bearer ")) {
            return bearer.replace("Bearer ", "")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * refresh token 으로 검증된 데이터 반환
     */
    fun getValidatedAuthDataByRefreshToken(request: HttpServletRequest, response: HttpServletResponse): String {
        val refresh = request.cookies.firstOrNull { it.name == "X-AUTH-REFRESH-TOKEN" }?.value
            ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        val tJwtAuthOptional = tJwtAuthRepository.findById(refresh)
        if (tJwtAuthOptional.isPresent) {
            val tJwtAuth = tJwtAuthOptional.get()
            if (!tJwtAuth.expired && tJwtAuth.expirationDate.isAfter(LocalDateTime.now())
            ) {
                // reIssue
                return reIssue(tJwtAuth, response)
            }
        }
        throw BaseException(BaseResponseCode.INVALID_TOKEN)
    }

    private fun setRefreshTokenToCookie(tJwtAuth: TJwtAuth, response: HttpServletResponse) {
        val cookie = Cookie("X-AUTH-REFRESH-TOKEN", tJwtAuth.refreshToken)
        cookie.maxAge = ChronoUnit.SECONDS.between(LocalDateTime.now(), tJwtAuth.expirationDate).toInt()
        cookie.path = "/"
        cookie.isHttpOnly = true
        response.addCookie(cookie)
    }
}
