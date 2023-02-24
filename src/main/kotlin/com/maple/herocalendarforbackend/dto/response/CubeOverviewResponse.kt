package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class CubeOverviewResponse(
    val registeredApiKeyCount: Long?,
    val counts: CubeCount,
)
