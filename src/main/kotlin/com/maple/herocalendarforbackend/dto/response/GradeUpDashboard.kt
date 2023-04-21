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
import com.maple.herocalendarforbackend.code.MagicVariables.SUSANG_ADDITIONAL_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.code.MagicVariables.SUSANG_EPIC_GRADE_UP
import com.maple.herocalendarforbackend.entity.IGradeUpCount
import lombok.Builder
import kotlin.math.roundToInt

@Suppress("MagicNumber")
@Builder
data class GradeUpDashboard(
    val epic: GradeUp,
    val unique: GradeUp,
    val legendary: GradeUp
) {
    companion object {
        private fun getGradeUpPercentage(beforeInfo: IGradeUpCount?, afterInfo: IGradeUpCount?): Double {
            val upgradeCount = (afterInfo?.getUpgradeSumCount() ?: 0).toDouble()
            val allCount = upgradeCount + (beforeInfo?.getSumCount()
                ?: 0) - (beforeInfo?.getUpgradeSumCount() ?: 0)

            return if (upgradeCount > 0)
                (upgradeCount.div(allCount) * 100000).roundToInt() / 1000.0
            else 0.0
        }

        fun convertSusang(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), SUSANG_EPIC_GRADE_UP),
                unique = GradeUp(0.0, 0.0),
                legendary = GradeUp(0.0, 0.0),
            )
        }

        fun convertSusangAdditional(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), SUSANG_ADDITIONAL_EPIC_GRADE_UP),
                unique = GradeUp(0.0, 0.0),
                legendary = GradeUp(0.0, 0.0),
            )
        }

        fun convertJangyin(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }
            val uniqueInfo = data.firstOrNull { it.getGrade() == "유니크" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), JANGYIN_EPIC_GRADE_UP),
                unique = GradeUp(getGradeUpPercentage(epicInfo, uniqueInfo), JANGYIN_UNIQUE_GRADE_UP),
                legendary = GradeUp(0.0, 0.0)
            )
        }

        fun convertMyungjang(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }
            val uniqueInfo = data.firstOrNull { it.getGrade() == "유니크" }
            val legendaryInfo = data.firstOrNull { it.getGrade() == "레전드리" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), MYUNGJANG_EPIC_GRADE_UP),
                unique = GradeUp(getGradeUpPercentage(epicInfo, uniqueInfo), MYUNGJANG_UNIQUE_GRADE_UP),
                legendary = GradeUp(getGradeUpPercentage(uniqueInfo, legendaryInfo), MYUNGJANG_LEGENDARY_GRADE_UP)
            )
        }

        fun convertRed(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }
            val uniqueInfo = data.firstOrNull { it.getGrade() == "유니크" }
            val legendaryInfo = data.firstOrNull { it.getGrade() == "레전드리" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), RED_EPIC_GRADE_UP),
                unique = GradeUp(getGradeUpPercentage(epicInfo, uniqueInfo), RED_UNIQUE_GRADE_UP),
                legendary = GradeUp(getGradeUpPercentage(uniqueInfo, legendaryInfo), RED_LEGENDARY_GRADE_UP)
            )
        }

        fun convertBlack(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }
            val uniqueInfo = data.firstOrNull { it.getGrade() == "유니크" }
            val legendaryInfo = data.firstOrNull { it.getGrade() == "레전드리" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), BLACK_EPIC_GRADE_UP),
                unique = GradeUp(getGradeUpPercentage(epicInfo, uniqueInfo), BLACK_UNIQUE_GRADE_UP),
                legendary = GradeUp(getGradeUpPercentage(uniqueInfo, legendaryInfo), BLACK_LEGENDARY_GRADE_UP)
            )
        }

        fun convertAdditional(data: List<IGradeUpCount>): GradeUpDashboard {
            val rareInfo = data.firstOrNull { it.getGrade() == "레어" }
            val epicInfo = data.firstOrNull { it.getGrade() == "에픽" }
            val uniqueInfo = data.firstOrNull { it.getGrade() == "유니크" }
            val legendaryInfo = data.firstOrNull { it.getGrade() == "레전드리" }

            return GradeUpDashboard(
                epic = GradeUp(getGradeUpPercentage(rareInfo, epicInfo), ADDITIONAL_EPIC_GRADE_UP),
                unique = GradeUp(getGradeUpPercentage(epicInfo, uniqueInfo), ADDITIONAL_UNIQUE_GRADE_UP),
                legendary = GradeUp(getGradeUpPercentage(uniqueInfo, legendaryInfo), ADDITIONAL_LEGENDARY_GRADE_UP)
            )
        }
    }
}
