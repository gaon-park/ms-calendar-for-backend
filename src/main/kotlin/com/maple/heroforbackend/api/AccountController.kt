package com.maple.heroforbackend.api

import com.maple.heroforbackend.config.JwtTokenProvider
import com.maple.heroforbackend.dto.request.AccountRegistRequest
import com.maple.heroforbackend.dto.request.LoginRequest
import com.maple.heroforbackend.dto.response.LoginResponse
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.EmailSendService
import com.maple.heroforbackend.service.EmailTokenService
import com.maple.heroforbackend.service.LoginService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountController(
    private val accountService: AccountService,
    private val loginService: LoginService,
    private val emailTokenService: EmailTokenService,
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
     * 데이터 INSERT 후, 인증 메일 발송
     */
    @PostMapping("/account/regist")
    fun regist(
        @Valid @RequestBody request: AccountRegistRequest
    ): ResponseEntity<LoginResponse> {
        val user = accountService.insert(request)
        user.id?.let {
            emailTokenService.sendEmailToken(it, user.email)
        }
        return login(LoginRequest(request.email, request.password))
    }

    /**
     * 이메일 인증
     */
    @GetMapping("confirm-email")
    fun confirmEmail(
        @Valid @RequestParam token: String
    ): ResponseEntity<String> {
        emailTokenService.verifyEmail(token)
        return ResponseEntity.ok("success!")
    }
}
