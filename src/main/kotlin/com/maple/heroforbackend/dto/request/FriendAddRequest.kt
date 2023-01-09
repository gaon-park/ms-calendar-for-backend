package com.maple.heroforbackend.dto.request

import jakarta.validation.constraints.NotEmpty

data class FriendAddRequest(
    @field:NotEmpty
    val personalKey: String
)
