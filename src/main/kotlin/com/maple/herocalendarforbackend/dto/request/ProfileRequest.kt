package com.maple.herocalendarforbackend.dto.request

import lombok.Builder

@Builder
data class ProfileRequest(
    val nickName: String,
    val isPublic: Boolean,
)
