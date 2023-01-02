package com.maple.heroforbackend.service

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.entity.TJwtAuth
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.repository.TJwtAuthRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtAuthService(
    private val userDetailsService: UserDetailsService,
    private val tJwtAuthRepository: TJwtAuthRepository
) {
    companion object {
        private var secretKey: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

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

        // refresh token insert to db
        val tJwtAuth = TJwtAuth.generate(accessToken, LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()))
        tJwtAuthRepository.save(tJwtAuth)

        // refresh token insert to httpOnly cookie
        setRefreshTokenToCookie(tJwtAuth, response)

        return tJwtAuth
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    fun reIssue(tJwtAuth: TJwtAuth, response: HttpServletResponse): TJwtAuth {
        val userPk = getAuthentication(tJwtAuth.accessKey).name
        var newAuth: TJwtAuth? = null
        userPk?.let {
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
    fun getValidatedAuthData(request: HttpServletRequest, response: HttpServletResponse): TJwtAuth? {
        val access = request.getHeader("X-AUTH-ACCESS-TOKEN")
        if (access != null && validateAccessToken(access)) {
            val tJwtAuth = tJwtAuthRepository.findByAccessKey(access)
                ?: throw BaseException(BaseResponseCode.INVALID_AUTH_TOKEN)
            if (tJwtAuth.id == request.cookies.firstOrNull { it.name == "X-AUTH-REFRESH-TOKEN" }?.value) {
                // reIssue
                return if (!tJwtAuth.expired && tJwtAuth.expirationDate.isAfter(LocalDateTime.now())) {
                    tJwtAuth
                } else {
                    reIssue(tJwtAuth, response)
                }
            }
        }
        return null
    }

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
