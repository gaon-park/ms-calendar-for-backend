package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class AlertsResponse(
    val waitingFollowerRequests: List<WaitingFollower>
)
