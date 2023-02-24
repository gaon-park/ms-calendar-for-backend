package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.ICubeTypeCount
import com.maple.herocalendarforbackend.entity.ICubeTypeItemCount
import lombok.Builder

@Builder
data class CubeCount(
    val allCount: Long,
    val susangCount: Long,
    val jangyinCount: Long,
    val myungjangCount: Long,
    val redCount: Long,
    val blackCount: Long,
    val additionalCount: Long,
) {
    companion object {
        fun convert(cubeCounts: List<ICubeTypeCount>) = CubeCount(
            allCount = cubeCounts.sumOf { it.getCount() },
            susangCount = cubeCounts.firstOrNull { it.getCubeType() == "SUSANG" }?.getCount() ?: 0,
            jangyinCount = cubeCounts.firstOrNull { it.getCubeType() == "JANGYIN" }?.getCount() ?: 0,
            myungjangCount = cubeCounts.firstOrNull { it.getCubeType() == "MYUNGJANG" }?.getCount() ?: 0,
            redCount = cubeCounts.firstOrNull { it.getCubeType() == "RED" }?.getCount() ?: 0,
            blackCount = cubeCounts.firstOrNull { it.getCubeType() == "BLACK" }?.getCount() ?: 0,
            additionalCount = cubeCounts.firstOrNull { it.getCubeType() == "ADDITIONAL" }?.getCount() ?: 0
        )

        fun convertFromItemCount(cubeCounts: List<ICubeTypeItemCount>) = CubeCount(
            allCount = cubeCounts.sumOf { it.getCount() },
            susangCount = cubeCounts.firstOrNull { it.getCubeType() == "SUSANG" }?.getCount() ?: 0,
            jangyinCount = cubeCounts.firstOrNull { it.getCubeType() == "JANGYIN" }?.getCount() ?: 0,
            myungjangCount = cubeCounts.firstOrNull { it.getCubeType() == "MYUNGJANG" }?.getCount() ?: 0,
            redCount = cubeCounts.firstOrNull { it.getCubeType() == "RED" }?.getCount() ?: 0,
            blackCount = cubeCounts.firstOrNull { it.getCubeType() == "BLACK" }?.getCount() ?: 0,
            additionalCount = cubeCounts.firstOrNull { it.getCubeType() == "ADDITIONAL" }?.getCount() ?: 0
        )
    }
}
