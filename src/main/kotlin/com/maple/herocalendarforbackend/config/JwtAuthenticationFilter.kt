package com.maple.herocalendarforbackend.config

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.service.JwtAuthService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.io.IOException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.security.SignatureException

@Suppress("TooGenericExceptionCaught")
class JwtAuthenticationFilter(
    private val jwtAuthService: JwtAuthService,
) : OncePerRequestFilter() {

    companion object {
        val EXCLUDE_URL = listOf(
            "/static/",
            "/favicon.ico",
            "/account/regist",
            "/confirm-email",
            "/index",
            "/login",
            "/search/",
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            if (request.servletPath.equals("/reissue/access-token")) {
                val token = jwtAuthService.getValidatedAuthDataByRefreshToken(request, response)
                request.setAttribute("accessToken", token)
                SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthentication(token)
            } else {
                val token = jwtAuthService.getValidatedAuthData(request)
                SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthentication(token)
            }
            filterChain.doFilter(request, response)
        } catch (exception: Exception) {
            when (exception) {
                is ExpiredJwtException -> setErrorResponse(response, BaseResponseCode.TOKEN_EXPIRED)
                is MalformedJwtException,
                is JwtException,
                is SignatureException,
                is IllegalArgumentException -> setErrorResponse(response, BaseResponseCode.INVALID_TOKEN)
            }
        }
    }

    // filtering 제외 URL 설정
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return EXCLUDE_URL.any { exclude ->
            request.servletPath.startsWith(exclude)
        }
    }

    private fun setErrorResponse(
        response: HttpServletResponse,
        baseResponseCode: BaseResponseCode,
    ) {
        response.status = baseResponseCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        try {
            response.writer.write(jsonMapper().writeValueAsString(baseResponseCode))
        } catch (_: IOException) { }
    }
}
