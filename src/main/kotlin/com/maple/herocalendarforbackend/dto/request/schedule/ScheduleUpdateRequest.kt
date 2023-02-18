package com.maple.herocalendarforbackend.dto.request.schedule

import com.maple.herocalendarforbackend.code.ScheduleUpdateCode
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import lombok.Builder
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@Builder
data class ScheduleUpdateRequest(
    @field:NotNull
    val scheduleId: Long,
    @field:NotEmpty
    val title: String,
    @field:NotNull
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val start: LocalDateTime,
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val end: LocalDateTime?,
    val allDay: Boolean,
    val memberIds: List<String>?,
    val note: String?,
    val isPublic: Boolean,
    val scheduleUpdateCode: ScheduleUpdateCode,
    val forOfficial: Boolean,
) {
    @AssertTrue
    fun isStart(): Boolean {
        return when {
            (end == null && allDay) -> true
            (end == null) -> false
            else -> end.isAfter(start)
        }
    }
}
