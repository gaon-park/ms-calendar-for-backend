package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class CubeItemData(
    val item: String,
    val cubeType: String,
    val categories: List<String>,
    val data: List<CubeEventRecordResponse>,
)
