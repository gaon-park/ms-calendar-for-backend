package com.maple.herocalendarforbackend.dto.request.friend

import jakarta.validation.constraints.NotEmpty

data class FriendRequest(
    @field:NotEmpty
    val personalKey: String,
)
