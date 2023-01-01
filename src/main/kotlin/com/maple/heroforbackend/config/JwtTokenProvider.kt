package com.maple.heroforbackend.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    private val userDetailsService: UserDetailsService
) {
    companion object {
        private var secretKey: Key = Keys.hmacShaKeyFor("Zr4u7x!A%D*F-JaNdRgUkXp2s5v8y/B?".toByteArray())

        /**
         * 토큰 유효시간 30분
         */
        private const val tokenValidTime = 30 * 60 * 1000L
    }

    /**
     * JWT 토큰 생성
     */
    fun createToken(userPk: String, roles: List<String>): String {
        val claims = Jwts.claims().setSubject(userPk)
        claims["roles"] = roles
        val now = Date()
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenValidTime))
            .signWith(secretKey)
            .compact()
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
     * Request 의 Header 에서 token 값을 가져옴
     * "X-AUTH-TOKEN": "값"
     */
    fun resolveToken(request: HttpServletRequest?) = request?.getHeader("X-AUTH-TOKEN")

    /**
     * 토큰의 유효성 + 만료일자 확인
     */
    fun validateToken(jwtToken: String): Boolean {
        val claims = Jwts
            .parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(jwtToken)
        return !claims.body.expiration.before(Date())
    }
}
