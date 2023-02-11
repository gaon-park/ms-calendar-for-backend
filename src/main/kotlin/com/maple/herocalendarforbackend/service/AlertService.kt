package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.dto.response.WaitingFollower
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val tFriendshipRepository: TFriendshipRepository,
) {
    fun findWaitingRequests(userId: String): AlertsResponse {
        val waitingFollowers = tFriendshipRepository.findWaitingRequest(userId)
        return AlertsResponse(
            waitingFollowerRequests = waitingFollowers.map {
                WaitingFollower.convert(it)
            }
        )
    }
}
