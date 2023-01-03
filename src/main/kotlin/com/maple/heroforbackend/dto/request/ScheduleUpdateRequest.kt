package com.maple.heroforbackend.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ScheduleUpdateRequest(
    @NotEmpty(message = "타이틀을 필수 항목입니다.")
    val title: String,
    @NotNull(message = "시작 날짜는 필수 항목입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val start: LocalDateTime,
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val end: LocalDateTime?,
    val allDay: Boolean?,
    val note: String?,
    val color: String?,
    val isPublic: Boolean?,
)
