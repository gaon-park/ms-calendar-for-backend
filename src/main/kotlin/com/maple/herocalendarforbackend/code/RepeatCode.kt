package com.maple.herocalendarforbackend.code

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.EnumUtils

@Suppress("MagicNumber")
enum class RepeatCode(
    val stepDay: Long?,
    val stepMonth: Long?,
    val value: String,
) {
    DAYS(1, null, "day"),
    WEEKS(7, null, "week"),
    MONTHS(null, 1, "month"),
    YEARS(365, 12, "year");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(value: String?): RepeatCode? =
            value?.let { EnumUtils.getEnumIgnoreCase(RepeatCode::class.java, it.trim()) }
    }
}
