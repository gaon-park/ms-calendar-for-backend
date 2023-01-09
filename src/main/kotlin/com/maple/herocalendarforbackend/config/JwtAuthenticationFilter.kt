package com.maple.herocalendarforbackend.config

import com.maple.herocalendarforbackend.service.JwtAuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

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
        val tJwtAuth = jwtAuthService.getValidatedAuthData(request, response)
        if (tJwtAuth != null) {
            SecurityContextHolder.getContext().authentication = jwtAuthService.getAuthentication(tJwtAuth.accessKey)
        }
        filterChain.doFilter(request, response)
    }

    // filtering 제외 URL 설정
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return EXCLUDE_URL.any { exclude ->
            request.servletPath.startsWith(exclude)
        }
    }
}
