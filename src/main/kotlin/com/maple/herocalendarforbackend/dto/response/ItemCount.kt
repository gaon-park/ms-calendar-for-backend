package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class ItemCount(
    val item: String,
    val count: Int,
)
