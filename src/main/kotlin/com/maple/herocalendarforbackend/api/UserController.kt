package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.AvatarImgRequest
import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.ProfileResponse
import com.maple.herocalendarforbackend.service.AlertService
import com.maple.herocalendarforbackend.service.UserService
import com.maple.herocalendarforbackend.util.ImageUtil
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
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
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
        summary = "get unconfirmed request list", description = "まだ承認・拒否してないRequest一覧を取得する API"
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
    @Operation(
        summary = "put user profile", description = "ログインユーザの加入情報を修正する API <br/>" +
                "修正したデータだけ送信（もし、修正内側がニックネームだけなら、ニックネームだけのJson送信）"
    )
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
        @Valid @RequestBody requestBody: ProfileRequest,
    ): ResponseEntity<ProfileResponse> =
        ResponseEntity.ok(
            ProfileResponse.convert(userService.updateProfile(principal.name, requestBody))
        )

    @Operation(
        summary = "get user profile image to base64 encoded string", description = "ログインユーザの画像ファイル取得 API <br/>" +
                "プロフィール画像修正時に使うデータ"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = String::class)))
            )
        ]
    )
    @PostMapping(
        "/encodedImg",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun getByteImg(
        @ModelAttribute request: AvatarImgRequest,
    ): ResponseEntity<String> =
        ResponseEntity.ok(
            ImageUtil().toByteString(request.avatarImg)
        )

    /**
     * 해당 accountId 값이 사용되고 있는지(변경 가능한 값인지 확인)
     */
    @Operation(
        summary = "is this account_id duplicated?",
        description = "該当AccountIdに変更できるか確認する API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = Boolean::class)))
            )
        ]
    )
    @GetMapping("/validate/{accountId}")
    fun accountIdDuplicateCheck(
        @PathVariable(name = "accountId") accountId: String,
    ): ResponseEntity<Boolean> =
        ResponseEntity.ok(userService.accountIdDuplicateCheck(accountId))
}
