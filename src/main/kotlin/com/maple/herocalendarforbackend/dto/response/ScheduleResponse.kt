package com.maple.herocalendarforbackend.dto.response

import lombok.Builder
import java.time.LocalDateTime

@Builder
data class ScheduleResponse(
    val scheduleId: Long,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val allDay: Boolean,
    val url: String,
    val note: String,
    val members: List<UserResponse>,
)
