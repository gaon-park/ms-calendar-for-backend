package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotEmpty

data class FriendAddRequest(
    @field:NotEmpty
    val personalKey: String
)
