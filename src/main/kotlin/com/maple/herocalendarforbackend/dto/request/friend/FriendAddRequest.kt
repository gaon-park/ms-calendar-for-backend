package com.maple.herocalendarforbackend.dto.request.friend

import jakarta.validation.constraints.NotEmpty

data class FriendAddRequest(
    @field:NotEmpty
    val personalKey: String,
    val note: String?
)
