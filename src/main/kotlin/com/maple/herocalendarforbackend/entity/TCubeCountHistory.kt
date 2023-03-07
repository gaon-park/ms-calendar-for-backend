package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "t_cube_count_history")
data class TCubeCountHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: String,
    val potentialOptionGrade: String,
    val additionalPotentialOptionGrade: String,
    val cubeType: String,
    val targetItem: String,
    val createdAt: LocalDate,
    val count: Int,
    val upgradeCount: Int,
) {

    companion object {
        fun convert(
            userId: String,
            date: LocalDate,
            cc: CubeCountUpgrade
        ) =
            TCubeCountHistory(
                userId = userId,
                potentialOptionGrade = cc.potentialOptionGrade,
                additionalPotentialOptionGrade = cc.additionalPotentialOptionGrade,
                cubeType = cc.cubeType,
                targetItem = cc.targetItem,
                createdAt = date,
                count = cc.count,
                upgradeCount = cc.upgradeCount
            )

        fun convertFromNextData(
            userId: String,
            date: LocalDate,
            cc: FromNexonData,
            count: Int,
            upgradeCount: Int,
        ) = TCubeCountHistory(
            userId = userId,
            potentialOptionGrade = cc.potentialOptionGrade,
            additionalPotentialOptionGrade = cc.additionalPotentialOptionGrade,
            cubeType = cc.cubeType,
            targetItem = cc.targetItem,
            createdAt = date,
            count = count,
            upgradeCount = upgradeCount
        )
    }

    data class CubeCountUpgrade(
        val potentialOptionGrade: String,
        val additionalPotentialOptionGrade: String,
        val targetItem: String,
        val cubeType: String,
        val count: Int,
        val upgradeCount: Int
    )

    data class FromNexonData(
        val targetItem: String,
        val cubeType: String,
        val potentialOptionGrade: String,
        val additionalPotentialOptionGrade: String
    ) {
        override fun equals(other: Any?): Boolean {
            val o = other as FromNexonData
            return when {
                o.targetItem != targetItem -> false
                o.cubeType != cubeType -> false
                o.potentialOptionGrade != potentialOptionGrade -> false
                o.additionalPotentialOptionGrade != additionalPotentialOptionGrade -> false
                else -> true
            }
        }

        override fun hashCode(): Int {
            var result = targetItem.hashCode()
            result = 31 * result + cubeType.hashCode()
            result = 31 * result + potentialOptionGrade.hashCode()
            result = 31 * result + additionalPotentialOptionGrade.hashCode()
            return result
        }
    }
}
