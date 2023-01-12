package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.dto.request.AccountRegistRequest
import com.maple.herocalendarforbackend.dto.request.LoginRequest
import com.maple.herocalendarforbackend.dto.response.LoginResponse
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.EmailTokenService
import com.maple.herocalendarforbackend.service.LoginService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Regist/Login", description = "Regist, Login関連 API")
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class AccountController(
    private val accountService: AccountService,
    private val loginService: LoginService,
    private val emailTokenService: EmailTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtAuthService: JwtAuthService
) {

    /**
     * 로그인, JWT TOKEN 발행
     */
    @SecurityRequirements(value = [])
    @Operation(summary = "Login", description = "Login API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(Content(schema = Schema(implementation = LoginResponse::class)))
            ),
            ApiResponse(
                responseCode = "404",
                description = "ユーザー情報がない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "400",
                description = "パスワードが一致してない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
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
    @Operation(summary = "JWT関連", description = "access_tokenが無効になった時、保存中のrefresh_tokenでaccess_tokenを再発行 API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(Content(schema = Schema(implementation = LoginResponse::class)))
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. ReadOnly CookieにRefreshToken情報がない<br>" +
                        "1. 該当するRefreshTokenがない",
                content = arrayOf(
                    Content(schema = Schema(implementation = ErrorResponse::class))
                )
            )
        ]
    )
    @GetMapping("/reissue/access-token")
    fun accessTokenReIssue(request: HttpServletRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(
            LoginResponse(request.getAttribute("accessToken").toString())
        )
    }

    /**
     * 데이터 save 후, 인증 메일 발송
     */
    @SecurityRequirements(value = [])
    @Operation(summary = "新規加入", description = "データ入力後、認定メール発送（会員加入未完了状態）")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
            ),
            ApiResponse(
                responseCode = "400",
                description = "すでに登録できているEmailと重ねる",
                content = arrayOf(
                    Content(schema = Schema(implementation = ErrorResponse::class))
                )
            )
        ]
    )
    @PostMapping("/account/regist")
    fun regist(
        @Valid @RequestBody request: AccountRegistRequest
    ) {
        val user = accountService.save(request)
        user.id?.let {
            emailTokenService.sendEmailToken(it, user.email)
        }
    }

    /**
     * 이메일 인증 완료 설정
     */
    @SecurityRequirements(value = [])
    @Operation(summary = "EMAIL認定", description = "加入情報入力時のEMAILに送られたTOKEN情報で会員加入を確定 API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
            ),
            ApiResponse(
                responseCode = "401",
                description = "無効なTOKEN",
                content = arrayOf(
                    Content(schema = Schema(implementation = ErrorResponse::class))
                )
            )
        ]
    )
    @GetMapping("/confirm-email")
    fun confirmEmail(
        @Valid @RequestParam token: String,
    ) {
        emailTokenService.verifyEmail(token)
    }
}
