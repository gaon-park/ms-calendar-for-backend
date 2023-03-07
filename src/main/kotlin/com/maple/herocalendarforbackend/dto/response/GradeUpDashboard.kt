package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.MagicVariables.ADDITIONAL_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.ADDITIONAL_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.BLACK_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.BLACK_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.RED_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.RED_UNIQUE_GRADE_UP
import lombok.Builder

@Builder
data class GradeUpDashboard(
    val actualRed: Double,
    val actualBlack: Double,
    val actualAdditional: Double,

    val expectedRed: Double,
    val expectedBlack: Double,
    val expectedAdditional: Double,
) {
    companion object {
        fun convert(
            actualRed: Double,
            actualBlack: Double,
            actualAdditional: Double,
            grade: String
        ) = GradeUpDashboard(
            actualRed, actualBlack, actualAdditional,
            expectedRed = if (grade == "레전드리") RED_LEGENDARY_GRADE_UP
            else RED_UNIQUE_GRADE_UP,
            expectedBlack = if (grade == "레전드리") BLACK_LEGENDARY_GRADE_UP
            else BLACK_UNIQUE_GRADE_UP,
            expectedAdditional = if (grade == "레전드리") ADDITIONAL_LEGENDARY_GRADE_UP
            else ADDITIONAL_UNIQUE_GRADE_UP
        )
    }
}
