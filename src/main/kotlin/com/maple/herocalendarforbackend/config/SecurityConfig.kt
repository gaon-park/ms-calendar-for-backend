package com.maple.herocalendarforbackend.config

import com.maple.herocalendarforbackend.service.JwtAuthService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthService: JwtAuthService
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                // todo 본방 개시 전, 삭제
//            .requestMatchers("/swagger/**", "/api-docs", "/api-docs/**")
            .requestMatchers("/", "/**")
            .hasRole("ADMIN")
            .and()
            .httpBasic()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests() // 요청에 대한 권한 체크
            .requestMatchers("/api/user", "/api/user/**").hasRole("USER")
            .anyRequest().permitAll() // 그 외 나머지 요청은 누구나 접근 가능
            .and()
            .addFilterBefore(
                JwtAuthenticationFilter(jwtAuthService),
                UsernamePasswordAuthenticationFilter::class.java
            )
        return http.build()
    }

    @Bean
    fun authenticationManager(auth: AuthenticationConfiguration): AuthenticationManager {
        return auth.authenticationManager
    }

    // todo 본방 개시 전, 삭제
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
