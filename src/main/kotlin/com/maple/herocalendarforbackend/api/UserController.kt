package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.ProfileResponse
import com.maple.herocalendarforbackend.service.AlertService
import com.maple.herocalendarforbackend.service.UserService
import com.maple.herocalendarforbackend.util.MapleGGUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "User tools", description = "Login User関連 API")
@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val alertService: AlertService,
    private val userService: UserService,
) {

    /**
     * 유저의 미응답 요청 리스트 검색
     */
    @Operation(
        summary = "get unconfirmed request list", description = "まだ承認・拒否してないRequest一覧を取得する API <br>" +
                "すでに相手からFriend Requestを受けている状態なら、承認にする"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = AlertsResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @GetMapping("/alerts")
    fun getAlerts(
        principal: Principal,
    ): ResponseEntity<AlertsResponse> = ResponseEntity.ok(
        alertService.findWaitingRequests(principal.name)
    )

    /**
     * 로그인 유저 프로필 정보
     */
    @Operation(summary = "get user profile", description = "ログインユーザの加入情報を確認する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = ProfileResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "404",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @GetMapping("/profile")
    fun getProfile(
        principal: Principal,
    ): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(
            ProfileResponse.convert(userService.findById(principal.name))
        )

    /**
     * 로그인 유저 프로필 수정
     */
    @Operation(summary = "put user profile", description = "ログインユーザの加入情報を修正する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = ProfileResponse::class)))
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "404",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @PutMapping("/profile")
    fun putProfile(
        principal: Principal,
        @Valid @RequestBody requestBody: ProfileRequest
    ): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(
            ProfileResponse.convert(userService.updateProfile(principal.name, requestBody))
        )

    @GetMapping("/reload/avatarImg")
    fun avatar(
        principal: Principal,
    ): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(
            ProfileResponse.convert(userService.reloadAvatarImg(principal.name))
        )
}
