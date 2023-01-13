package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class UserResponse(
    val id: String?,
    val email: String,
    val nickName: String,
)
