package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.dto.nexon.CubeHistoryDTO
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_cube_history")
data class TCubeHistory(
    @Id
    val id: ByteArray,
    val userId: String,
    val cubeType: String,
    val itemUpgrade: Boolean,
    val miracleFlg: Boolean,
    val itemLevel: Int,
    val targetItem: String,
    val potentialOptionGrade: String,
    val additionalPotentialOptionGrade: String,

    val beforeOption1: String?,
    val beforeOption2: String?,
    val beforeOption3: String?,

    val beforeOptionValue1: String?,
    val beforeOptionValue2: String?,
    val beforeOptionValue3: String?,

    val afterOption1: String?,
    val afterOption2: String?,
    val afterOption3: String?,

    val afterOptionValue1: String?,
    val afterOptionValue2: String?,
    val afterOptionValue3: String?,

    val beforeAdditionalOption1: String?,
    val beforeAdditionalOption2: String?,
    val beforeAdditionalOption3: String?,

    val beforeAdditionalOptionValue1: String?,
    val beforeAdditionalOptionValue2: String?,
    val beforeAdditionalOptionValue3: String?,

    val afterAdditionalOption1: String?,
    val afterAdditionalOption2: String?,
    val afterAdditionalOption3: String?,

    val afterAdditionalOptionValue1: String?,
    val afterAdditionalOptionValue2: String?,
    val afterAdditionalOptionValue3: String?,

    val createdAt: LocalDateTime,
) {
    companion object {
        @Suppress("LongMethod")
        fun convert(
            userId: String, data: CubeHistoryDTO,
        ): TCubeHistory {
            var beforeOptions: List<String?> = listOf(null, null, null)
            var beforeOptionValues: List<String?> = listOf(null, null, null)
            var afterOptions: List<String?> = listOf(null, null, null)
            var afterOptionValues: List<String?> = listOf(null, null, null)
            var beforeAdditionalOptions: List<String?> = listOf(null, null, null)
            var beforeAdditionalOptionValues: List<String?> = listOf(null, null, null)
            var afterAdditionalOptions: List<String?> = listOf(null, null, null)
            var afterAdditionalOptionValues: List<String?> = listOf(null, null, null)

            if (data.cubeType != "에디셔널 큐브") {
                val bOptions = listOf(
                    data.beforePotentialOptions.getOrNull(0)?.value?.split(":"),
                    data.beforePotentialOptions.getOrNull(1)?.value?.split(":"),
                    data.beforePotentialOptions.getOrNull(2)?.value?.split(":"),
                )
                beforeOptions = listOf(
                    bOptions[0]?.getOrNull(0)?.trimEnd(),
                    bOptions[1]?.getOrNull(0)?.trimEnd(),
                    bOptions[2]?.getOrNull(0)?.trimEnd(),
                )
                beforeOptionValues = listOf(
                    bOptions[0]?.getOrNull(1)?.trimStart(),
                    bOptions[1]?.getOrNull(1)?.trimStart(),
                    bOptions[2]?.getOrNull(1)?.trimStart(),
                )

                val aOptions = listOf(
                    data.afterPotentialOptions.getOrNull(0)?.value?.split(":"),
                    data.afterPotentialOptions.getOrNull(1)?.value?.split(":"),
                    data.afterPotentialOptions.getOrNull(2)?.value?.split(":"),
                )
                afterOptions = listOf(
                    aOptions[0]?.getOrNull(0)?.trimEnd(),
                    aOptions[1]?.getOrNull(0)?.trimEnd(),
                    aOptions[2]?.getOrNull(0)?.trimEnd(),
                )
                afterOptionValues = listOf(
                    aOptions[0]?.getOrNull(1)?.trimStart(),
                    aOptions[1]?.getOrNull(1)?.trimStart(),
                    aOptions[2]?.getOrNull(1)?.trimStart(),
                )
            } else {
                val bOptions = listOf(
                    data.beforeAdditionalPotentialOptions.getOrNull(0)?.value?.split(":"),
                    data.beforeAdditionalPotentialOptions.getOrNull(1)?.value?.split(":"),
                    data.beforeAdditionalPotentialOptions.getOrNull(2)?.value?.split(":"),
                )
                beforeAdditionalOptions = listOf(
                    bOptions[0]?.getOrNull(0)?.trimEnd(),
                    bOptions[1]?.getOrNull(0)?.trimEnd(),
                    bOptions[2]?.getOrNull(0)?.trimEnd(),
                )
                beforeAdditionalOptionValues = listOf(
                    bOptions[0]?.getOrNull(1)?.trimEnd(),
                    bOptions[1]?.getOrNull(1)?.trimEnd(),
                    bOptions[2]?.getOrNull(1)?.trimEnd(),
                )

                val aOptions = listOf(
                    data.afterAdditionalPotentialOptions.getOrNull(0)?.value?.split(":"),
                    data.afterAdditionalPotentialOptions.getOrNull(1)?.value?.split(":"),
                    data.afterAdditionalPotentialOptions.getOrNull(2)?.value?.split(":"),
                )
                afterAdditionalOptions = listOf(
                    aOptions[0]?.getOrNull(0)?.trimEnd(),
                    aOptions[1]?.getOrNull(0)?.trimEnd(),
                    aOptions[2]?.getOrNull(0)?.trimEnd(),
                )
                afterAdditionalOptionValues = listOf(
                    aOptions[0]?.getOrNull(1)?.trimEnd(),
                    aOptions[1]?.getOrNull(1)?.trimEnd(),
                    aOptions[2]?.getOrNull(1)?.trimEnd(),
                )
            }

            return TCubeHistory(
                id = data.id.toByteArray(),
                userId = userId,
                cubeType = data.cubeType,
                itemUpgrade = data.itemUpgradeResult != "실패",
                miracleFlg = data.miracleTimeFlag != "이벤트 적용되지 않음",
                itemLevel = data.itemLevel,
                targetItem = data.targetItem,
                potentialOptionGrade = data.potentialOptionGrade,
                additionalPotentialOptionGrade = data.additionalPotentialOptionGrade,

                beforeOption1 = beforeOptions.getOrNull(0),
                beforeOption2 = beforeOptions.getOrNull(1),
                beforeOption3 = beforeOptions.getOrNull(2),

                beforeOptionValue1 = beforeOptionValues.getOrNull(0),
                beforeOptionValue2 = beforeOptionValues.getOrNull(1),
                beforeOptionValue3 = beforeOptionValues.getOrNull(2),

                afterOption1 = afterOptions.getOrNull(0),
                afterOption2 = afterOptions.getOrNull(1),
                afterOption3 = afterOptions.getOrNull(2),

                afterOptionValue1 = afterOptionValues.getOrNull(0),
                afterOptionValue2 = afterOptionValues.getOrNull(1),
                afterOptionValue3 = afterOptionValues.getOrNull(2),

                beforeAdditionalOption1 = beforeAdditionalOptions.getOrNull(0),
                beforeAdditionalOption2 = beforeAdditionalOptions.getOrNull(1),
                beforeAdditionalOption3 = beforeAdditionalOptions.getOrNull(2),

                beforeAdditionalOptionValue1 = beforeAdditionalOptionValues.getOrNull(0),
                beforeAdditionalOptionValue2 = beforeAdditionalOptionValues.getOrNull(1),
                beforeAdditionalOptionValue3 = beforeAdditionalOptionValues.getOrNull(2),

                afterAdditionalOption1 = afterAdditionalOptions.getOrNull(0),
                afterAdditionalOption2 = afterAdditionalOptions.getOrNull(1),
                afterAdditionalOption3 = afterAdditionalOptions.getOrNull(2),

                afterAdditionalOptionValue1 = afterAdditionalOptionValues.getOrNull(0),
                afterAdditionalOptionValue2 = afterAdditionalOptionValues.getOrNull(1),
                afterAdditionalOptionValue3 = afterAdditionalOptionValues.getOrNull(2),

                createdAt = data.createDate
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TCubeHistory

        if (!id.contentEquals(other.id)) return false

        return true
    }

    override fun hashCode(): Int {
        return id.contentHashCode()
    }
}
