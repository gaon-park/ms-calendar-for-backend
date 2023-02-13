package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import org.springframework.stereotype.Service

@Service
class AlertService {
    @Suppress("UnusedPrivateMember")
    fun findWaitingRequests(userId: String): AlertsResponse {
        return AlertsResponse(
            waitingFollowerRequests = emptyList()
        )
    }
}
