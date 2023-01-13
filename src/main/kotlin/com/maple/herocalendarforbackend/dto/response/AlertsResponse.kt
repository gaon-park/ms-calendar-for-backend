package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class AlertsResponse(
    val ownerChangesRequests: List<WaitingOwnerChange>,
    val waitingScheduleRequests: List<WaitingSchedule>,
    val waitingFriendRequests: List<WaitingFriend>
)
