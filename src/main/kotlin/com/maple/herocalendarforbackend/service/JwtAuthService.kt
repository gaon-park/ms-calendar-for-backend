package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.MagicVariables.AUTHORIZATION_REFRESH_JWT
import com.maple.herocalendarforbackend.code.MagicVariables.JWT_ACCESS_TOKEN_EXPIRATION_TIME_VALUE
import com.maple.herocalendarforbackend.dto.response.LoginResponse
import com.maple.herocalendarforbackend.entity.TJwtAuth
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.properties.AppProperties
import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtAuthService(
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tUserRepository: TUserRepository,
    appProperties: AppProperties,
) {
    private var secretKey = Keys.hmacShaKeyFor(appProperties.jwtSecretKey.toByteArray())

    /**
     * 최초 로그인 토큰 생성
     */
    fun firstTokenForLogin(email: String, roles: List<String>, response: HttpServletResponse): LoginResponse {
        val tUser = tUserRepository.findByEmail(email) ?: throw BaseException(
            BaseResponseCode.USER_NOT_FOUND
        )
        tUser.id?.let {
            return createToken(tUser, roles, response)
        } ?: throw BaseException(BaseResponseCode.DATA_ERROR)
    }

    /**
     * JWT 토큰 생성
     */
    @Transactional
    fun createToken(userPk: TUser, roles: List<String>, response: HttpServletResponse): LoginResponse {
        // delete exist tokens
        userPk.id?.let {
            tJwtAuthRepository.deleteByUserPk(it)
            tJwtAuthRepository.flush()
        }

        // create access token
        val claims = Jwts.claims().setSubject(userPk.id)
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
        setCookie(tJwtAuth, response)
        return LoginResponse(accessToken)
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
     * DB 를 거치지 않고 토큰에서 갑ㅇ슬 꺼내 바로 시큐리티 유저 객체를 만듦
     */
    fun getAuthentication(token: String): Authentication {
        val claimSubject = getUserPkOnValidatedAccessToken(token)
        val principal = User(claimSubject, "", AuthorityUtils.createAuthorityList("ROLE_USER"))
        return UsernamePasswordAuthenticationToken(principal, "", AuthorityUtils.createAuthorityList("ROLE_USER"))
    }

    /**
     * 검증된 데이터 반환
     */
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
    fun getValidatedAuthDataByRefreshToken(request: HttpServletRequest, response: HttpServletResponse): LoginResponse {
        val refresh = try {
            request.cookies.firstOrNull { it.name == AUTHORIZATION_REFRESH_JWT }?.value
        } catch (_: java.lang.NullPointerException) {
            throw BaseException(BaseResponseCode.NO_REFRESH_TOKEN)
        }

        refresh?.let {
            val tJwtAuthOptional = tJwtAuthRepository.findById(it)
            if (tJwtAuthOptional.isPresent) {
                val tJwtAuth = tJwtAuthOptional.get()
                if (!tJwtAuth.expired && tJwtAuth.expirationDate.isAfter(LocalDateTime.now())
                ) {
                    // reIssue
                    return createToken(tJwtAuth.userPk, listOf("ROLE_USER"), response)
                }
            }
        }
        throw BaseException(BaseResponseCode.INVALID_TOKEN)
    }

    fun setCookie(tJwtAuth: TJwtAuth, response: HttpServletResponse) {
        tJwtAuth.refreshToken?.let {
            val refreshCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_JWT, it)
                .path("/")
                .httpOnly(true)
                .maxAge(ChronoUnit.SECONDS.between(LocalDateTime.now(), tJwtAuth.expirationDate))
                .build()
            response.addHeader("Set-Cookie", refreshCookie.toString())
        }
    }
}
