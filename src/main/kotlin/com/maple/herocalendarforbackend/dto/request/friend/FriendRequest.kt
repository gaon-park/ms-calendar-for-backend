package com.maple.herocalendarforbackend.dto.request.friend

import jakarta.validation.constraints.NotEmpty
import lombok.Builder

@Builder
data class FriendRequest(
    @field:NotEmpty
    val personalKey: String,
)
