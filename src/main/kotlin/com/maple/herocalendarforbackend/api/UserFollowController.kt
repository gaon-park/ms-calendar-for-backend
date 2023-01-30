package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.follow.FollowRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.service.FollowRelationshipService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Follow/Follower CURD", description = "ユーザのFollow/Follower追加、更新、閲覧、削除関連 API")
@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserFollowController(
    private val followRelationshipService: FollowRelationshipService
) {

    /**
     * 팔로우 요청
     */
    @Operation(summary = "send a follow request", description = "ユーザ個人キーでfollow requestを送る API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
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
            )
        ]
    )
    @PostMapping("/follow")
    fun follow(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followRelationshipService.followRequest(principal.name, requestBody)
    }

    /**
     * 팔로우 취소
     */
    @Operation(summary = "follow cancel", description = "ユーザ個人キーで該当ユーザをfollowをキャンセルする API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
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
            )
        ]
    )
    @DeleteMapping("/follow/{personalKey}")
    fun followCancel(
        principal: Principal,
        @PathVariable(name = "personalKey") personalKey: String,
    ) {
        followRelationshipService.followCancel(principal.name, personalKey)
    }

    /**
     * 팔로우 요청 수락
     */
    @Operation(summary = "follow request accept", description = "follow Requestを受け取る API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
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
            )
        ]
    )
    @PutMapping("/follow/accept")
    fun followRequestAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followRelationshipService.followRequestAccept(requestBody.personalKey, principal.name)
    }

    /**
     * 팔로우 요청 거절
     */
    @Operation(summary = "follow request refuse", description = "follow Requestを断る API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
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
            )
        ]
    )
    @PutMapping("/follow/refuse")
    fun followRequestRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followRelationshipService.followRequestRefuse(requestBody.personalKey, principal.name)
    }


    /**
     * 로그인 유저가 팔로잉 중인 유저의 리스트
     */
    @Operation(summary = "get followings", description = "ログインユーザがFollowしているユーザ一覧を取得する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(
                    Content(array = ArraySchema(schema = Schema(implementation = UserResponse::class)))
                )
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @GetMapping("/followings")
    fun getFollowings(
        principal: Principal
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            followRelationshipService.findFollowings(principal.name)
        )
    }

    /**
     * 로그인 유저를 팔로우하는 유저의 리스트
     */
    @Operation(summary = "get followers", description = "ログインユーザをFollowしているユーザ一覧を取得する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(
                    Content(array = ArraySchema(schema = Schema(implementation = UserResponse::class)))
                )
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
        ]
    )
    @GetMapping("/followers")
    fun getFollowers(
        principal: Principal
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            followRelationshipService.findFollowers(principal.name)
        )
    }
}
