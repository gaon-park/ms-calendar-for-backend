package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.ICubeTypeItemCount
import lombok.Builder

@Builder
data class CubeItemCount(
    val item: String,
    val cubeCount: CubeCount
)
