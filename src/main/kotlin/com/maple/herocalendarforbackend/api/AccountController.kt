package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.dto.request.AccountRegistRequest
import com.maple.herocalendarforbackend.dto.request.LoginRequest
import com.maple.herocalendarforbackend.dto.response.LoginResponse
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.EmailTokenService
import com.maple.herocalendarforbackend.service.LoginService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
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
    private val jwtAuthService: JwtAuthService
) {

    /**
     * 최초 로그인, JWT TOKEN 발행
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> = with(loginService.loadUserByUsername(request.email)) {
        if (this == null) {
            throw BaseException(BaseResponseCode.USER_NOT_FOUND)
        } else if (!passwordEncoder.matches(request.password, this.password)) {
            throw BaseException(BaseResponseCode.INVALID_PASSWORD)
        } else {
            ResponseEntity.ok(
                LoginResponse(
                    jwtAuthService.firstTokenForLogin(
                        request.email,
                        SecurityContextHolder.getContext().authentication.authorities.mapNotNull {
                            "ROLE_${it.authority}"
                        },
                        response
                    )
                )
            )
        }
    }

    /**
     * refresh token 으로 access token 재발급
     */
    @GetMapping("/reissue/access-token")
    fun accessTokenReIssue(request: HttpServletRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(
            LoginResponse(request.getAttribute("accessToken").toString())
        )
    }

    /**
     * 데이터 save 후, 인증 메일 발송
     */
    @PostMapping("/account/regist")
    fun regist(
        @Valid @RequestBody request: AccountRegistRequest
    ): ResponseEntity<String> {
        val user = accountService.save(request)
        user.id?.let {
            emailTokenService.sendEmailToken(it, user.email)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 이메일 인증 완료 설정
     */
    @GetMapping("/confirm-email")
    fun confirmEmail(
        @Valid @RequestParam token: String,
    ): ResponseEntity<String> {
        emailTokenService.verifyEmail(token)
        return ResponseEntity.ok("인증 성공! 이제 로그인할 수 있습니다!")
    }
}
