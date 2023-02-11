package com.maple.herocalendarforbackend.dto.request

import lombok.Builder

@Builder
data class PageInfo(
    val limit: Int?,
    val offset: Int?,
)
