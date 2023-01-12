package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.WaitingFriendRequest
import com.maple.herocalendarforbackend.dto.response.WaitingScheduleRequest
import com.maple.herocalendarforbackend.service.AlertService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "User tools", description = "Login User関連 API")
@RestController
@RequestMapping("/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val alertService: AlertService,
) {

    /**
     * 유저의 미응답 요청 리스트 검색
     */
    @Operation(summary = "get unconfirmed request list", description = "まだ承認・拒否してないRequest一覧を取得する API <br>" +
            "すでに相手からFriend Requestを受けている状態なら、承認にする")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(Content(schema = Schema(implementation = AlertsResponse::class)))
            )
        ]
    )
    @GetMapping("/alerts")
    fun getAlerts(
        principal: Principal,
    ): ResponseEntity<AlertsResponse> = ResponseEntity.ok(
        AlertsResponse(
            waitingScheduleRequests = alertService.findWaitingScheduleRequests(principal.name)
                .map { r -> WaitingScheduleRequest.convert(r) },
            waitingFriendRequests = alertService.findWaitingFriendRequests(principal.name)
                .map { r -> WaitingFriendRequest.convert(r) }
        )
    )
}
