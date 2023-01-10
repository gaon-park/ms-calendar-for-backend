package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class AlertsResponse(
    val waitingScheduleRequests: List<WaitingScheduleRequest>,
    val waitingFriendRequests: List<WaitingFriendRequest>
)
