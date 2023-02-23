package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class WholeRecordDashboardResponse(
    val categories: List<String>,
    val data: List<CubeEventRecordResponse>
)
