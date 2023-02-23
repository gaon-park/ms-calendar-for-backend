package com.maple.herocalendarforbackend.entity

import java.time.LocalDate

interface IWholeRecordDashboardDate {
    fun getDate(): LocalDate
    fun getCubeType(): String
    fun getCount(): Int
}
