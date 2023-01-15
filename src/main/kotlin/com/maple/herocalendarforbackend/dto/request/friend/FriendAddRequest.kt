package com.maple.herocalendarforbackend.dto.request.friend

import jakarta.validation.constraints.NotEmpty
import lombok.Builder

@Builder
data class FriendAddRequest(
    @field:NotEmpty
    val personalKey: String,
    val note: String?
)
