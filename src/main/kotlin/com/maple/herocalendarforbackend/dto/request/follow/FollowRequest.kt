package com.maple.herocalendarforbackend.dto.request.follow

import lombok.Builder

@Builder
data class FollowRequest(
    val personalKey: String,
)
