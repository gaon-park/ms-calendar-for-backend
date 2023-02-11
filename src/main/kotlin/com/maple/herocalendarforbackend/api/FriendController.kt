package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.PageInfo
import com.maple.herocalendarforbackend.dto.request.friend.FriendRequest
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Friend CURD", description = "ユーザのFriend追加、更新、閲覧、削除関連 API")
@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class FriendController(
    private val friendshipService: FriendshipService
) {

    /**
     * send friend request
     */
    @Operation(summary = "send a friend request", description = "ユーザ個人キーでfriend requestを送る API")
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
    @PostMapping("/friend")
    fun friendRequest(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.request(principal.name, requestBody)
    }

    /**
     * delete friend
     */
    @Operation(summary = "follow cancel", description = "友達関係をやめる API")
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
    @PutMapping("/friend/delete")
    fun deleteFriend(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.delete(principal.name, requestBody.personalKey)
    }

    /**
     * accept friend request
     */
    @Operation(summary = "follow request accept", description = "friend Requestを受け取る API")
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
    @PutMapping("/friend/accept")
    fun acceptFriendRequest(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.acceptRequest(requestBody.personalKey, principal.name)
    }

    /**
     * refuse friend request
     */
    @Operation(summary = "friend request refuse", description = "friend Requestを断る API")
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
    @PutMapping("/friend/refuse")
    fun friendRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ) {
        friendshipService.delete(requestBody.personalKey, principal.name)
    }

    @Operation(summary = "get friends", description = "ステータスに関係なく、ログインユーザの友達リストを取得する API")
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
    @GetMapping("/friend")
    fun getFriends(
        principal: Principal,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            friendshipService.findAllStatusFriends(
                principal.name,
                PageInfo.convert(limit, offset)
            )
        )
    }
}
