package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotEmpty

data class FriendRequest(
    @field:NotEmpty
    val personalKey: String,
)
