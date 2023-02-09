package com.maple.herocalendarforbackend.config

import ch.qos.logback.classic.LoggerContext
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
import org.slf4j.Logger
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

    private val context = LoggerContext()
    private val logger: Logger = context.getLogger(JwtAuthenticationFilter::class.java)

    companion object {
        val EXCLUDE_URL = listOf(
            "/static/",
            "/favicon.ico",
            "/api/oauth2/"
        )
        val GET_BOTH_URL = listOf(
            "/api/search/user",
            "/api/search/schedule",
            "/api/schedule"
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
                    logger.info("optional principal")
                } catch (e: Exception) {
                    logger.info("optional principal exception!: " + e.cause)
                    logger.info("but this area is skip exception ^^")
                }
            } else if (request.servletPath.equals("/api/reissue/token")) {
                val token = jwtAuthService.getValidatedAuthDataByRefreshToken(request, response)
                request.setAttribute("accessToken", token)
                SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthentication(token)
            } else {
                val token = jwtAuthService.getValidatedAuthData(request)
                SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthentication(token)
            }
            filterChain.doFilter(request, response)
        } catch (exception: Exception) {
            logger.info("principal exception!: " + exception.cause)
            logger.info(exception.stackTrace)
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
        logger.info("needOptionalPrincipal check")
        val resURL = GET_BOTH_URL.any { url -> (request.servletPath.equals(url)) }
            val resMethod = (request.method.equals("GET", true))
        val url: String = request.servletPath
        logger.info("url:$url")
        logger.info("resURL:$resURL")
        logger.info("resMethod:$resMethod")
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
