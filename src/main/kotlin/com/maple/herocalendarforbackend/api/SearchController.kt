package com.maple.herocalendarforbackend.api

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

@Tag(name = "Search", description = "検索関連(ログアウト状態でもアクセス可) API")
@RestController
@RequestMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
class SearchController(
    private val searchService: SearchService
) {

    /**
     * email/nickName 으로 publicUser 검색
     */
    @SecurityRequirements(value = [])
    @Operation(summary = "ユーザ検索", description = "Email/NickNameで公開ユーザを検索(部分一致) API")
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
    fun findPublicByEmailOrNickName(@RequestParam(name = "user") user: String): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(
            searchService.findPublicByEmailOrNickName(user).map {
                UserResponse(it.email, it.nickName)
            }
        )
}
