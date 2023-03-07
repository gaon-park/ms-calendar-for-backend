package com.maple.herocalendarforbackend.entity

import java.time.LocalDate

interface ICubeCountBatch {
    fun getUserId(): String
    fun getCreatedAt(): LocalDate
    fun getPotentialOptionGrade(): String
    fun getAdditionalPotentialOptionGrade(): String
    fun getCubeType(): String
    fun getTargetItem(): String
    fun getCount(): Int
    fun getItemUpgradeCount(): Int
}
