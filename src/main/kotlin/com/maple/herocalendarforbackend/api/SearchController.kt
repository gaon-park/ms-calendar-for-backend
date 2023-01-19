package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.service.SearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.LocalDate

@Tag(name = "Search", description = "検索関連(ログアウト状態でもアクセス) API")
@RestController
@RequestMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
class SearchController(
    private val searchService: SearchService
) {

    /**
     * email/nickName 으로 publicUser 검색
     */
    @SecurityRequirements(value = [])
    @Operation(
        summary = "ユーザ検索", description = "Email/NickNameで公開ユーザを検索(部分一致) API <br/>" +
                "ログインユーザの場合、友達関係である非公開ユーザを検索することが可能"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(
                    Content(array = ArraySchema(schema = Schema(implementation = UserResponse::class)))
                )
            )
        ]
    )
    @GetMapping("/user")
    fun findUserByEmailOrNickName(
        principal: Principal?,
        @RequestParam(name = "user") user: String
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            principal?.name?.let {
                searchService.findFriendByEmailOrNickName(it, user)
            } ?: searchService.findPublicByEmailOrNickName(user))
    }

    /**
     * 로그아웃 유저: 공개 유저의 공개 스케줄만
     * 로그인 유저: 친구이면 모든 스케줄 공개
     */
    @SecurityRequirements(value = [])
    @Operation(
        summary = "スケジュール検索", description = "ログアウトユーザは公開ユーザのスケジュールを、" +
                "ログインユーザは公開＋非公開友達ユーザのスケジュールを検索する API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(
                    Content(array = ArraySchema(schema = Schema(implementation = ScheduleResponse::class)))
                )
            )
        ]
    )
    @GetMapping("/user/schedule")
    fun findSchedules(
        principal: Principal?,
        @RequestParam(name = "userId") userId: String,
        @RequestParam from: LocalDate?,
        @RequestParam to: LocalDate?
    ): ResponseEntity<List<ScheduleResponse>> {
        return ResponseEntity.ok(
            principal?.name?.let {
                searchService.findFriendSchedules(
                    it, userId, from, to
                )
            } ?: searchService.findPublicUserSchedules(
                userId, from, to
            )
        )
    }
}
