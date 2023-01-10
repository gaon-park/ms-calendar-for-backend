package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.dto.response.WaitingFriendRequest
import com.maple.herocalendarforbackend.dto.response.WaitingScheduleRequest
import com.maple.herocalendarforbackend.service.AlertService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/user")
class UserController(
    private val alertService: AlertService
) {

    /**
     * 유저의 미응답 요청 리스트 검색
     */
    @GetMapping("/alerts")
    fun getAlerts(
        principal: Principal,
    ): ResponseEntity<Any> = ResponseEntity.ok(
        AlertsResponse(
            waitingScheduleRequests = alertService.findWaitingScheduleRequests(principal.name)
                .map { r -> WaitingScheduleRequest.convert(r) },
            waitingFriendRequests = alertService.findWaitingFriendRequests(principal.name)
                .map { r -> WaitingFriendRequest.convert(r) }
        )
    )
}
