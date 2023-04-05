package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class GradeUp(
    val actual: Double,
    val expected: Double,
)
