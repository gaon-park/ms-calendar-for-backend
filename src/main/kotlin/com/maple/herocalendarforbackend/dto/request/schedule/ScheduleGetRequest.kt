package com.maple.herocalendarforbackend.dto.request.schedule

import jakarta.validation.constraints.AssertTrue
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class ScheduleGetRequest(
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val from: LocalDate?,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val to: LocalDate?
) {
    @AssertTrue
    fun isFrom(): Boolean {
        if (to == null) {
            return true
        }
        return to.isAfter(from)
    }
}
