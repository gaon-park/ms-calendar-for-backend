package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.NotificationRequest
import com.maple.herocalendarforbackend.dto.request.user.APIKeyRequest
import com.maple.herocalendarforbackend.dto.request.user.ProfileRequest
import com.maple.herocalendarforbackend.dto.response.APIKeyResponse
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.dto.response.NotificationResponse
import com.maple.herocalendarforbackend.service.CubeService
import com.maple.herocalendarforbackend.service.NotificationService
import com.maple.herocalendarforbackend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "User tools", description = "Login User関連 API")
@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val userService: UserService,
    private val notificationService: NotificationService,
    private val cubeService: CubeService
) {

    @GetMapping("/api-key")
    fun getApiKey(
        principal: Principal,
    ): ResponseEntity<APIKeyResponse?> {
        return ResponseEntity.ok(cubeService.getApiKey(principal.name))
    }

    @PostMapping("/api-key")
    fun setApiKey(
        principal: Principal,
        @Valid @RequestBody requestBody: APIKeyRequest
    ) {
        cubeService.registProcess(requestBody.apiKey, principal.name)
    }

    @Operation(
        summary = "get unconfirmed notification list"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = NotificationResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @GetMapping("/notifications")
    fun getNotifications(
        principal: Principal,
    ): ResponseEntity<List<NotificationResponse>> = ResponseEntity.ok(
        notificationService.findAll(principal.name)
    )

    @Operation(
        summary = "read one notification"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = String::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @PutMapping("/notification/read")
    fun readNotification(
        principal: Principal,
        @Valid @RequestBody requestBody: NotificationRequest
    ): ResponseEntity<String> {
        notificationService.deleteByRead(principal.name, requestBody.id)
        return ResponseEntity.ok("ok")
    }

    @Operation(
        summary = "read all notification"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = String::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @PutMapping("/notifications/read-all")
    fun readAllNotifications(
        principal: Principal,
    ): ResponseEntity<String> {
        notificationService.deleteByReadAllEvent(principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 로그인 유저 프로필 정보
     */
    @Operation(summary = "get user profile", description = "ログインユーザの加入情報を確認する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = IProfileResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping("/profile")
    fun getProfile(
        principal: Principal,
    ): ResponseEntity<IProfileResponse> =
        ResponseEntity.ok(
            userService.findByIdToIProfileResponse(principal.name)
        )

    /**
     * 로그인 유저 프로필 수정
     */
    @Operation(
        summary = "put user profile", description = "ログインユーザの加入情報を修正する API <br/>" +
                "修正したデータだけ送信（もし、修正内側がニックネームだけなら、ニックネームだけのJson送信）"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = IProfileResponse::class)))
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @PutMapping("/profile")
    fun putProfile(
        principal: Principal,
        @Valid @RequestBody requestBody: ProfileRequest,
    ): ResponseEntity<IProfileResponse> =
        ResponseEntity.ok(
            userService.updateProfile(principal.name, requestBody),
        )

    @Operation(
        summary = "deactivate account", description = "退会 API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = String::class)))
            )
        ]
    )
    @DeleteMapping
    fun deleteUser(
        principal: Principal
    ): ResponseEntity<String> {
        userService.deleteUser(principal.name)
        return ResponseEntity.ok("ok")
    }
}
