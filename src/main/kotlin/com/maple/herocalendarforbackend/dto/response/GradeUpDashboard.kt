package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.MagicVariables.ADDITIONAL_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.BLACK_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.RED_LEGENDARY_GRADE_UP
import lombok.Builder

@Builder
data class GradeUpDashboard(
    val actualRed: Double,
    val actualBlack: Double,
    val actualAdditional: Double,

    val expectedRed: Double = RED_LEGENDARY_GRADE_UP,
    val expectedBlack: Double = BLACK_LEGENDARY_GRADE_UP,
    val expectedAdditional: Double = ADDITIONAL_LEGENDARY_GRADE_UP,
)
