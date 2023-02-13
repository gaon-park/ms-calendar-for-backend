package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_LENGTH_OF_USER_COLUMN
import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.SearchUserResponse
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

@Suppress("LongParameterList")
@Tag(name = "Search", description = "検索関連(ログアウト状態でもアクセス) API")
@RestController
@RequestMapping("/api/search", produces = [MediaType.APPLICATION_JSON_VALUE])
class SearchController(
    private val searchService: SearchService
) {

    /**
     * accountId 로 user 검색
     */
    @Operation(
        summary = "ユーザ検索", description = "accountIdでユーザを検索(部分一致)する API <br/>" +
                "最大検索結果制限: 「100件」"
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
    fun findUser(
        principal: Principal,
        @RequestParam("keyword", required = false) keyword: String?,
        @RequestParam("world", required = false) world: String?,
        @RequestParam("job", required = false) job: String?,
        @RequestParam("jobDetail", required = false) jobDetail: String?,
    ): ResponseEntity<SearchUserResponse> {
        return ResponseEntity.ok(
            searchService.findUser(
                principal.name, SearchUserRequest(
                    if (keyword != null && keyword.length > MAX_LENGTH_OF_USER_COLUMN) keyword.substring(
                        MAX_LENGTH_OF_USER_COLUMN
                    )
                    else keyword,
                    world,
                    job,
                    jobDetail,
                )
            )
        )
    }

    /**
     * 공개 유저이거나 친구인 유저의 공개 스케줄
     */
    @Operation(
        summary = "本人じゃないユーザのスケジュール取得", description = "公開＋友達関係ユーザの公開スケジュールを検索する API"
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
    @GetMapping("/schedule")
    fun findSchedules(
        principal: Principal,
        @RequestParam(name = "userId") userId: String,
        @RequestParam from: LocalDate,
        @RequestParam to: LocalDate
    ): ResponseEntity<List<ScheduleResponse>> {
        return ResponseEntity.ok(
            searchService.findUserSchedules(
                loginUserId = principal.name,
                targetUserId = userId,
                from = from,
                to = to
            )
        )
    }
}
