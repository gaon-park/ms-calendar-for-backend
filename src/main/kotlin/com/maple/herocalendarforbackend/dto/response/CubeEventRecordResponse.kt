package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.IWholeRecordDashboardDate
import com.maple.herocalendarforbackend.entity.IWholeRecordDashboardMonth
import lombok.Builder
import java.time.format.DateTimeFormatter

@Builder
data class CubeEventRecordResponse(
    val category: String,
    val cubeType: String,
    val count: Int
) {
    companion object {
        fun convertMonth(data: List<IWholeRecordDashboardMonth>): List<CubeEventRecordResponse> {
            val category = "${data[0].getYear()}/${data[0].getMonth()}"

            // red, black, additional 3
            val red = data.firstOrNull { it.getCubeType() == "레드 큐브" }
            val black = data.firstOrNull { it.getCubeType() == "블랙 큐브" }
            val additional = data.firstOrNull { it.getCubeType() == "에디셔널 큐브" }

            val redR = CubeEventRecordResponse(category, "레드 큐브", red?.getCount() ?: 0)
            val blackR = CubeEventRecordResponse(category, "블랙 큐브", black?.getCount() ?: 0)
            val additionalR = CubeEventRecordResponse(category, "에디셔널 큐브", additional?.getCount() ?: 0)

            return listOf(redR, blackR, additionalR)
        }

        fun convertDate(data: List<IWholeRecordDashboardDate>): List<CubeEventRecordResponse> {
            val category = data[0].getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) ?: ""

            // red, black, additional 3
            val red = data.firstOrNull { it.getCubeType() == "레드 큐브" }
            val black = data.firstOrNull { it.getCubeType() == "블랙 큐브" }
            val additional = data.firstOrNull { it.getCubeType() == "에디셔널 큐브" }

            val redR = CubeEventRecordResponse(category, "레드 큐브", red?.getCount() ?: 0)
            val blackR = CubeEventRecordResponse(category, "블랙 큐브", black?.getCount() ?: 0)
            val additionalR = CubeEventRecordResponse(category, "에디셔널 큐브", additional?.getCount() ?: 0)

            return listOf(redR, blackR, additionalR)
        }
    }
}
