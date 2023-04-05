package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.MagicVariables.ADDITIONAL_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.ADDITIONAL_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.ADDITIONAL_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.BLACK_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.BLACK_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.BLACK_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.JANGYIN_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.JANGYIN_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.MYUNGJANG_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.MYUNGJANG_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.MYUNGJANG_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.RED_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.RED_LEGENDARY_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.RED_UNIQUE_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.SUSANG_EPIC_GRADE_UP
import lombok.Builder

@Builder
data class GradeUpDashboard(
    val susang: GradeUp,
    val jangyin: GradeUp,

    val myungjang: GradeUp,
    val red: GradeUp,
    val black: GradeUp,
    val additional: GradeUp,
) {
    companion object {
        fun convertLegendary(
            actualMyungjang: Double,
            actualRed: Double,
            actualBlack: Double,
            actualAdditional: Double,
        ) = GradeUpDashboard(
            susang = GradeUp(0.0, 0.0),
            jangyin = GradeUp(0.0, 0.0),
            myungjang = GradeUp(actualMyungjang, MYUNGJANG_LEGENDARY_GRADE_UP),
            red = GradeUp(actualRed, RED_LEGENDARY_GRADE_UP),
            black = GradeUp(actualBlack, BLACK_LEGENDARY_GRADE_UP),
            additional = GradeUp(actualAdditional, ADDITIONAL_LEGENDARY_GRADE_UP)
        )

        fun convertUnique(
            actualJangyin: Double,
            actualMyungjang: Double,
            actualRed: Double,
            actualBlack: Double,
            actualAdditional: Double,
        ) = GradeUpDashboard(
            susang = GradeUp(0.0, 0.0),
            jangyin = GradeUp(actualJangyin, JANGYIN_UNIQUE_GRADE_UP),
            myungjang = GradeUp(actualMyungjang, MYUNGJANG_UNIQUE_GRADE_UP),
            red = GradeUp(actualRed, RED_UNIQUE_GRADE_UP),
            black = GradeUp(actualBlack, BLACK_UNIQUE_GRADE_UP),
            additional = GradeUp(actualAdditional, ADDITIONAL_UNIQUE_GRADE_UP)
        )

        @Suppress("LongParameterList")
        fun convertEpic(
            actualSusang: Double,
            actualJangyin: Double,
            actualMyungjang: Double,
            actualRed: Double,
            actualBlack: Double,
            actualAdditional: Double,
        ) = GradeUpDashboard(
            susang = GradeUp(actualSusang, SUSANG_EPIC_GRADE_UP),
            jangyin = GradeUp(actualJangyin, JANGYIN_EPIC_GRADE_UP),
            myungjang = GradeUp(actualMyungjang, MYUNGJANG_EPIC_GRADE_UP),
            red = GradeUp(actualRed, RED_EPIC_GRADE_UP),
            black = GradeUp(actualBlack, BLACK_EPIC_GRADE_UP),
            additional = GradeUp(actualAdditional, ADDITIONAL_EPIC_GRADE_UP)
        )
    }
}
