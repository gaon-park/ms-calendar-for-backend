package com.maple.herocalendarforbackend.dto.request.friend

import lombok.Builder

@Builder
data class FollowRequest(
    val personalKey: String,
)
