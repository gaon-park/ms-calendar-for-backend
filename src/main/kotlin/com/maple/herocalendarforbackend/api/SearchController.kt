package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_LENGTH_OF_USER_COLUMN
import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.dto.response.SimpleUserResponse
import com.maple.herocalendarforbackend.entity.IProfile
import com.maple.herocalendarforbackend.service.SearchService
import io.swagger.v3.oas.annotations.Operation
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

@Suppress("LongParameterList")
@Tag(name = "Search", description = "検索関連(ログアウト状態でもアクセス) API")
@RestController
@RequestMapping("/api/search", produces = [MediaType.APPLICATION_JSON_VALUE])
@SecurityRequirements(value = [])
class SearchController(
    private val searchService: SearchService
) {

    @Operation(
        summary = "ユーザ検索（検索画面用）", description = "ユーザを検索(部分一致)する API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(
                    Content(schema = Schema(implementation = IProfile::class))
                )
            )
        ]
    )
    @GetMapping("/user")
    fun findUser(
        principal: Principal?,
        @RequestParam("keyword", required = false) keyword: String?,
        @RequestParam("world", required = false) world: String?,
        @RequestParam("job", required = false) job: String?,
        @RequestParam("jobDetail", required = false) jobDetail: String?,
    ): ResponseEntity<List<IProfile>> {
        return ResponseEntity.ok(
            searchService.findUser(
                SearchUserRequest(
                    if (keyword != null && keyword.length > MAX_LENGTH_OF_USER_COLUMN) keyword.substring(
                        MAX_LENGTH_OF_USER_COLUMN
                    )
                    else keyword,
                    world,
                    job,
                    jobDetail,
                ),
                principal?.name,
            )
        )
    }

    @GetMapping("/schedule-invite-target")
    fun findUserListForScheduleSearch(
        principal: Principal?,
        @RequestParam("keyword", required = false) keyword: String?,
    ): ResponseEntity<List<SimpleUserResponse>> {
        return ResponseEntity.ok(
            searchService.findUserListForScheduleSearch(
                principal?.name,
                keyword
            )
        )
    }

    @Operation(
        summary = "ユーザ検索（プロフィール画面用）", description = "accountIdでユーザを検索(完全一致)する API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(
                    Content(schema = Schema(implementation = IProfileResponse::class))
                )
            )
        ]
    )
    @GetMapping("/user/profile")
    fun getUserProfile(
        principal: Principal?,
        @RequestParam("accountId") accountId: String
    ): ResponseEntity<IProfileResponse> {
        return ResponseEntity.ok(
            searchService.findUserProfileByAccountId(accountId, principal?.name)
        )
    }
}
