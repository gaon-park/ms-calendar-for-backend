package com.maple.heroforbackend.dto.response

import lombok.Builder

@Builder
data class UserResponse(
    val email: String,
    val nickName: String,
)
