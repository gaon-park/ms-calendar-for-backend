package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.friend.FollowRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.SearchUserResponse
import com.maple.herocalendarforbackend.service.FollowService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Follow/Follower CURD")
@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class FollowController(
    private val followService: FollowService
) {
    @Operation(summary = "send follow request")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PostMapping("/follow")
    fun followRequest(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followService.followRequest(
            loginUserId = principal.name,
            respondentId = requestBody.personalKey
        )
    }

    @Operation(summary = "follow cancel")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PutMapping("/follow/cancel")
    fun followCancel(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followService.followCancel(
            loginUserId = principal.name,
            respondentId = requestBody.personalKey
        )
    }

    @Operation(summary = "delete from my follower")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PutMapping("/follower/delete")
    fun deleteFromByFollower(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followService.deleteFromMyFollower(
            loginUserId = principal.name,
            followerId = requestBody.personalKey
        )
    }

    @Operation(summary = "follow request accept")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PutMapping("/follow/accept")
    fun requestAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: FollowRequest
    ) {
        followService.requestAccept(
            loginUserId = principal.name,
            followerId = requestBody.personalKey
        )
    }

    @Operation(summary = "find all follow")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = SearchUserResponse::class)))
            )
        ]
    )
    @GetMapping("/follow")
    fun findFollows(
        principal: Principal
    ): ResponseEntity<SearchUserResponse> {
        val follows = followService.findFollows(principal.name)
        return ResponseEntity.ok(
            SearchUserResponse(
                users = follows,
                fullHit = follows.size.toLong()
            )
        )
    }

    @Operation(summary = "find all follower")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(Content(schema = Schema(implementation = SearchUserResponse::class)))
            )
        ]
    )
    @GetMapping("/follower")
    fun findFollowers(
        principal: Principal
    ): ResponseEntity<SearchUserResponse> {
        val follower = followService.findFollowers(principal.name)
        return ResponseEntity.ok(
            SearchUserResponse(
                users = follower,
                fullHit = follower.size.toLong()
            )
        )
    }
}
