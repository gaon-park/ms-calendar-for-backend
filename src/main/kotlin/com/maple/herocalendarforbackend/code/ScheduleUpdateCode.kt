package com.maple.herocalendarforbackend.code

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.EnumUtils

@Suppress("MagicNumber")
enum class ScheduleUpdateCode(
    val id: Int,
    val rangeInfo: String,
) {
    ONLY_THIS(0, "이 일정만"),
    ALL(1, "모든 일정"),
    THIS_AND_FUTURE(2, "이 일정 및 향후 일정");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(value: String?): ScheduleUpdateCode? =
            value?.let { EnumUtils.getEnumIgnoreCase(ScheduleUpdateCode::class.java, it.trim()) }
    }
}
