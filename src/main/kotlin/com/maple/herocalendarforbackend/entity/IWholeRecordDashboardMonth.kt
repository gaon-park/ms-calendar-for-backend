package com.maple.herocalendarforbackend.entity

interface IWholeRecordDashboardMonth {
    fun getYear(): Int
    fun getMonth(): Int
    fun getCubeType(): String
    fun getCount(): Int
}
