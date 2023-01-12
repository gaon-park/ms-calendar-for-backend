package com.maple.herocalendarforbackend.dto.request

import com.maple.herocalendarforbackend.code.RepeatCode
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class RepeatInfo(
    val repeatCodeValue: RepeatCode,
    @field:NotNull
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val start: LocalDate,
    @field:NotNull
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val end: LocalDate
) {
    @AssertTrue
    fun isStart() = end.isAfter(start)
}
