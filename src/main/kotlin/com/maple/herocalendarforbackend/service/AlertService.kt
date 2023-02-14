package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.repository.TFollowRepository
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val tFollowRepository: TFollowRepository
) {
    fun findWaitingRequests(loginUserId: String): AlertsResponse {
        return AlertsResponse(
            waitingFollowerRequests = tFollowRepository.findUnRespondentRequest(loginUserId)
        )
    }
}
