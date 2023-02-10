package com.maple.herocalendarforbackend.dto.request.schedule

import lombok.Builder

@Builder
data class ReissueRequest(
    val refreshToken: String
)
