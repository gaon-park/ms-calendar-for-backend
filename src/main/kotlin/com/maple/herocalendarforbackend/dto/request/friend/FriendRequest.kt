package com.maple.herocalendarforbackend.dto.request.friend

import lombok.Builder

@Builder
data class FriendRequest(
    val personalKey: String,
)
