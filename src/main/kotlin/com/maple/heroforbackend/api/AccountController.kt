package com.maple.heroforbackend.api

import com.maple.heroforbackend.config.JwtTokenProvider
import com.maple.heroforbackend.dto.request.AccountRegistRequest
import com.maple.heroforbackend.dto.request.LoginRequest
import com.maple.heroforbackend.dto.response.LoginResponse
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.code.BaseResponseCode
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
    ): ResponseEntity<LoginResponse> = with(loginService.loadUserByUsername(request.email)) {
        if (this == null) {
            throw BaseException(BaseResponseCode.USER_NOT_FOUND)
        } else if (!passwordEncoder.matches(request.password, this.password)) {
            throw BaseException(BaseResponseCode.INVALID_PASSWORD)
        } else {
            ResponseEntity.ok(
                LoginResponse(
                    jwtTokenProvider.createToken(
                        this.username,
                        listOf("ROLE_USER")
                    )
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
        accountService.insert(request)
        return login(
            LoginRequest(request.email, request.password)
        )
    }
}
