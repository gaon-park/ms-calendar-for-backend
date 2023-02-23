package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
class ItemDashboardResponse(
    val itemList: List<String>,
    val cubeHistories: List<CubeHistoryResponse>
)
