package com.maple.herocalendarforbackend.dto.request.admin

import lombok.Builder
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Builder
data class DateRequest(
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
)
