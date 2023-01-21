package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class AlertsResponse(
    val waitingFriendRequests: List<WaitingFriend>
)
