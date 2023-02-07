package com.maple.herocalendarforbackend.dto.request.schedule

import jakarta.validation.constraints.AssertTrue
import lombok.Builder
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Builder
data class ScheduleGetRequest(
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val from: LocalDate,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val to: LocalDate
) {
    @AssertTrue
    fun isFrom(): Boolean {
        return to.isAfter(from) || to.isEqual(from);
    }
}
