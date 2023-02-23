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
            val red = data.firstOrNull { it.getCubeType() == "RED" }
            val black = data.firstOrNull { it.getCubeType() == "BLACK" }
            val additional = data.firstOrNull { it.getCubeType() == "ADDITIONAL" }

            val redR = CubeEventRecordResponse(category, "RED", red?.getCount() ?: 0)
            val blackR = CubeEventRecordResponse(category, "BLACK", black?.getCount() ?: 0)
            val additionalR = CubeEventRecordResponse(category, "ADDITIONAL", additional?.getCount() ?: 0)

            return listOf(redR, blackR, additionalR)
        }

        fun convertDate(data: List<IWholeRecordDashboardDate>): List<CubeEventRecordResponse> {
            val category = data[0].getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) ?: ""

            // red, black, additional 3
            val red = data.firstOrNull { it.getCubeType() == "RED" }
            val black = data.firstOrNull { it.getCubeType() == "BLACK" }
            val additional = data.firstOrNull { it.getCubeType() == "ADDITIONAL" }

            val redR = CubeEventRecordResponse(category, "RED", red?.getCount() ?: 0)
            val blackR = CubeEventRecordResponse(category, "BLACK", black?.getCount() ?: 0)
            val additionalR = CubeEventRecordResponse(category, "ADDITIONAL", additional?.getCount() ?: 0)

            return listOf(redR, blackR, additionalR)
        }
    }
}
