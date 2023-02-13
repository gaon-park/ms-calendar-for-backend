package com.maple.herocalendarforbackend.dto.response

data class SearchUserResponse(
    val users: List<UserResponse>,
    val fullHit: Long,
)
