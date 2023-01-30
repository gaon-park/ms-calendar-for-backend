package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.dto.response.WaitingFollower
import com.maple.herocalendarforbackend.repository.TFollowRelationshipRepository
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val tFollowRelationshipRepository: TFollowRelationshipRepository,
) {
    fun findWaitingRequests(userId: String): AlertsResponse {
        val waitingFollowers = tFollowRelationshipRepository.findWaitingFollowers(userId)
        return AlertsResponse(
            waitingFollowerRequests = waitingFollowers.map {
                WaitingFollower.convert(it)
            }
        )
    }
}
