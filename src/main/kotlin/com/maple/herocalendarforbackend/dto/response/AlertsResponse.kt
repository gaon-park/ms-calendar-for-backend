package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.IWaitingFollower
import lombok.Builder

@Builder
data class AlertsResponse(
    val waitingFollowerRequests: List<IWaitingFollower>
)
