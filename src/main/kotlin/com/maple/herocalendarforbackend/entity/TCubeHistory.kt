package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.nexon.CubeType
import com.maple.herocalendarforbackend.code.nexon.PotentialOption
import com.maple.herocalendarforbackend.dto.nexon.CubeHistoryDTO
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_cube_history")
data class TCubeHistory(
    @Id
    val id: String,
    val userId: String,
    @Enumerated(value = EnumType.STRING)
    val cubeType: CubeType?,
    val itemUpgrade: Boolean,
    val miracleFlg: Boolean,
    val itemLevel: Int,
    val targetItem: String,
    @Enumerated(value = EnumType.STRING)
    val potentialOptionGrade: PotentialOption?,
    @Enumerated(value = EnumType.STRING)
    val additionalPotentialOptionGrade: PotentialOption?,

    val afterOption1: String?,
    val afterOption2: String?,
    val afterOption3: String?,

    val afterAdditionalOption1: String?,
    val afterAdditionalOption2: String?,
    val afterAdditionalOption3: String?,

    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(
            userId: String, data: CubeHistoryDTO,
            cubeTypeMap: Map<String, CubeType>,
            potentialOptionMap: Map<String, PotentialOption>
        ) = TCubeHistory(
            id = data.id,
            userId = userId,
            cubeType = cubeTypeMap[data.cubeType],
            itemUpgrade = data.itemUpgradeResult != "실패",
            miracleFlg = data.miracleTimeFlag != "이벤트 적용되지 않음",
            itemLevel = data.itemLevel,
            targetItem = data.targetItem,
            potentialOptionGrade = potentialOptionMap[data.potentialOptionGrade],
            additionalPotentialOptionGrade = potentialOptionMap[data.potentialOptionGrade],

            afterOption1 = data.afterPotentialOptions.getOrNull(0)?.value,
            afterOption2 = data.afterPotentialOptions.getOrNull(1)?.value,
            afterOption3 = data.afterPotentialOptions.getOrNull(2)?.value,

            afterAdditionalOption1 = data.afterAdditionalPotentialOptions.getOrNull(0)?.value,
            afterAdditionalOption2 = data.afterAdditionalPotentialOptions.getOrNull(1)?.value,
            afterAdditionalOption3 = data.afterAdditionalPotentialOptions.getOrNull(2)?.value,

            createdAt = data.createDate
        )
    }
}
