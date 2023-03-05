package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.ICubeTypeCount
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
            susangCount = cubeCounts.firstOrNull { it.getCubeType() == "수상한 큐브" }?.getCount() ?: 0,
            jangyinCount = cubeCounts.firstOrNull { it.getCubeType() == "장인의 큐브" }?.getCount() ?: 0,
            myungjangCount = cubeCounts.firstOrNull { it.getCubeType() == "명장의 큐브" }?.getCount() ?: 0,
            redCount = cubeCounts.firstOrNull { it.getCubeType() == "레드 큐브" }?.getCount() ?: 0,
            blackCount = cubeCounts.firstOrNull { it.getCubeType() == "블랙 큐브" }?.getCount() ?: 0,
            additionalCount = cubeCounts.firstOrNull { it.getCubeType() == "에디셔널 큐브" }?.getCount() ?: 0
        )
    }
}
