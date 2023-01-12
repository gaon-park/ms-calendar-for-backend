package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.dto.request.FriendRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.service.FriendshipService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Friend CURD", description = "ユーザのFriend追加、更新、閲覧、削除関連 API")
@RestController
@RequestMapping("/user/friend", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserFriendController(
    private val friendshipService: FriendshipService
) {

    /**
     * 친구 요청 보내기
     */
    @Operation(summary = "send a friend request", description = "ユーザ個人キーでFriendRequestを送る API")
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
    @PostMapping("/add")
    fun addFriend(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendAddRequest
    ) {
        friendshipService.friendRequest(principal.name, requestBody)
    }

    /**
     * 친구 삭제
     */
    @Operation(summary = "delete friend", description = "ユーザ個人キーでFriend関係を削除する API")
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
    @DeleteMapping
    fun deleteFriend(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.deleteFriend(principal.name, requestBody)
    }

    /**
     * 친구 요청 수락
     */
    @Operation(summary = "friend request accept", description = "Friend Requestを受け取る API")
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
    @PutMapping("/accept")
    fun friendRequestAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.friendRequestAccept(requestBody.personalKey, principal.name)
    }

    /**
     * 친구 요청 거절
     */
    @Operation(summary = "friend request refuse", description = "Friend Requestを断る API")
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
    @PutMapping("/refuse")
    fun friendRequestRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.friendRequestRefuse(requestBody.personalKey, principal.name)
    }


    /**
     * 로그인 유저의 친구 리스트
     */
    @Operation(summary = "get friends", description = "承認済みのFriend一覧を取得する API")
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
    @GetMapping
    fun findFriends(
        principal: Principal
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            friendshipService.findFriends(principal.name).map {
                UserResponse(it.email, it.nickName)
            }
        )
    }
}
