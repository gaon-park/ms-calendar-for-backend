package com.maple.herocalendarforbackend.dto.request

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class ScheduleGetRequest(
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val from: LocalDate?,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val to: LocalDate?
)
