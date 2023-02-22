package com.maple.herocalendarforbackend.dto.request.user

import lombok.Builder

@Builder
data class APIKeyRequest(
    val apiKey: String,
)
