package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.LoginResponse
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.service.UserService
import com.maple.herocalendarforbackend.service.GoogleOAuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "認証まわり API")
@RestController
@RequestMapping("/api", produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController(
    private val googleOAuthService: GoogleOAuthService,
    private val jwtAuthService: JwtAuthService,
    private val userService: UserService,
) {

    /**
     * google oauth
     */
    @Operation(
        summary = "google oauth2 serverから得たcode値でログインに必要なRefresh/AccessTokenを取得"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = LoginResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @SecurityRequirements(value = [])
    @GetMapping("/oauth2/google")
    fun googleLogin(
        @RequestParam(name = "code") code: String,
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        val email = googleOAuthService.process(code)
        return with(userService.loadUserByUsername(email)) {
            this?.let {
                ResponseEntity.ok(
                    email?.let {
                        jwtAuthService.firstTokenForLogin(
                            it, authorities.mapNotNull { a -> a.authority },
                            response
                        )
                    }
                )
            } ?: throw BaseException(BaseResponseCode.USER_NOT_FOUND)
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
                content = arrayOf(Content(schema = Schema(implementation = LoginResponse::class)))
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(
                    Content(schema = Schema(implementation = ErrorResponse::class))
                )
            )
        ]
    )
    @PostMapping("/reissue/token")
    fun accessTokenReIssue(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(
            jwtAuthService.getValidatedAuthDataByRefreshToken(request, response)
        )
    }
}
