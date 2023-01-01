package com.maple.heroforbackend.api

import com.maple.heroforbackend.config.JwtTokenProvider
import com.maple.heroforbackend.dto.request.AccountRegistRequest
import com.maple.heroforbackend.dto.request.LoginRequest
import com.maple.heroforbackend.dto.response.LoginResponse
import com.maple.heroforbackend.exception.InvalidRequestException
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.LoginService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountController(
    private val accountService: AccountService,
    private val loginService: LoginService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 최초 로그인, JWT TOKEN 발행
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        message: String?
    ): ResponseEntity<LoginResponse> = with(loginService.loadUserByUsername(request.email)) {
        if (this == null) {
            throw InvalidRequestException("does not exist")
        } else if (!passwordEncoder.matches(request.password, this.password)) {
            throw InvalidRequestException("check your password")
        } else {
            ResponseEntity.ok(
                LoginResponse(
                    jwtTokenProvider.createToken(
                        this.username,
                        listOf("ROLE_USER")
                    ), message
                )
            )
        }
    }

    /**
     * 가입 후, JWT TOKEN 발행
     */
    @PostMapping("/account/regist")
    fun regist(
        @Valid @RequestBody request: AccountRegistRequest
    ): ResponseEntity<LoginResponse> {
        val message = if (accountService.insert(request).id != null) {
            null
        } else {
            "fail to regist"
        }
        return login(
            LoginRequest(request.email, request.password),
            message
        )
    }
}
