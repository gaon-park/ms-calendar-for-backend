package com.maple.herocalendarforbackend.config

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.exception.BaseException
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
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.SignatureException

@Component
@Suppress("TooGenericExceptionCaught")
class JwtAuthenticationFilter(
    private val jwtAuthService: JwtAuthService,
) : OncePerRequestFilter() {

    companion object {
        val EXCLUDE_URL = listOf(
            "/static/",
            "/favicon.ico",
            "/api/oauth2/",
            "/api/reissue/token",
            "/api/dashboard/item-options",
            "/api/dashboard/whole-record",
            "/api/dashboard/cube-overview",
            "/api/dashboard/grade-up/legendary",
            "/api/dashboard/grade-up/unique",
            "/api/dashboard/top-five",
        )
        val GET_BOTH_URL = listOf(
            "/api/search/user",
            "/api/search/user/profile",
            "/api/search/schedule-invite-target",
            "/api/schedule",
            "/api/schedule/other",
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            if (needOptionalPrincipal(request)) {
                try {
                    val token = jwtAuthService.getValidatedAuthData(request)
                    SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthentication(token)
                } catch (_: Exception) {
                }
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
                is BaseException,
                is IllegalArgumentException -> setErrorResponse(response, BaseResponseCode.INVALID_TOKEN)
                is java.lang.NullPointerException -> setErrorResponse(response, BaseResponseCode.UNAUTHORIZED)
            }
        }
    }

    fun needOptionalPrincipal(request: HttpServletRequest): Boolean {
        val resURL = GET_BOTH_URL.any { url -> (request.servletPath.equals(url)) }
        val resMethod = (request.method.equals("GET", true))
        return resURL && resMethod
    }

    // filtering 제외 URL 설정
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return EXCLUDE_URL.any { exclude ->
            (request.servletPath.startsWith(exclude) || request.servletPath.contains("api-docs")
                    || request.servletPath.contains("swagger"))
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
        } catch (_: IOException) {
        }
    }
}
