package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TCubeHistory
import lombok.Builder

@Suppress("MaxLineLength", "ComplexMethod")
@Builder
data class CubeHistoryResponse(
    val id: String,
    val targetItem: String,
    val cubeType: String,
    val beforeOption1: String,
    val beforeOption2: String,
    val beforeOption3: String,
    val afterOption1: String,
    val afterOption2: String,
    val afterOption3: String,
    val potentialOptionGrade: String,
    val itemUpgrade: Boolean,
) {
    companion object {
        fun convert(data: TCubeHistory): CubeHistoryResponse {
            val beforeOptions =
                if (data.cubeType != "에디셔널 큐브") {
                    listOf(
                        data.beforeOption1 + if (data.beforeOptionValue1 != null) " : " + data.beforeOptionValue1 else "",
                        data.beforeOption2 + if (data.beforeOptionValue2 != null) " : " + data.beforeOptionValue2 else "",
                        data.beforeOption3 + if (data.beforeOptionValue3 != null) " : " + data.beforeOptionValue3 else "",
                    )
                } else {
                    listOf(
                        data.beforeAdditionalOption1 + if (data.beforeAdditionalOptionValue1 != null) " : " + data.beforeAdditionalOptionValue1 else "",
                        data.beforeAdditionalOption2 + if (data.beforeAdditionalOptionValue2 != null) " : " + data.beforeAdditionalOptionValue2 else "",
                        data.beforeAdditionalOption3 + if (data.beforeAdditionalOptionValue3 != null) " : " + data.beforeAdditionalOptionValue3 else "",
                    )
                }
            val afterOptions =
                if (data.cubeType != "에디셔널 큐브") {
                    listOf(
                        data.afterOption1 + if (data.afterOptionValue1 != null) " : " + data.afterOptionValue1 else "",
                        data.afterOption2 + if (data.afterOptionValue2 != null) " : " + data.afterOptionValue2 else "",
                        data.afterOption3 + if (data.afterOptionValue3 != null) " : " + data.afterOptionValue3 else "",
                    )
                } else {
                    listOf(
                        data.afterAdditionalOption1 + if (data.afterAdditionalOptionValue1 != null) " : " + data.afterAdditionalOptionValue1 else "",
                        data.afterAdditionalOption2 + if (data.afterAdditionalOptionValue2 != null) " : " + data.afterAdditionalOptionValue2 else "",
                        data.afterAdditionalOption3 + if (data.afterAdditionalOptionValue3 != null) " : " + data.afterAdditionalOptionValue3 else "",
                    )
                }

            return CubeHistoryResponse(
                id = data.id.toString(),
                targetItem = data.targetItem,
                cubeType = data.cubeType,
                beforeOption1 = beforeOptions[0],
                beforeOption2 = beforeOptions[1],
                beforeOption3 = beforeOptions[2],
                afterOption1 = afterOptions[0],
                afterOption2 = afterOptions[1],
                afterOption3 = afterOptions[2],
                potentialOptionGrade = if (data.cubeType == "에디셔널 큐브") data.additionalPotentialOptionGrade else data.potentialOptionGrade,
                itemUpgrade = data.itemUpgrade
            )
        }
    }
}
