package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ScheduleAddRequest(
    @field:NotEmpty(message = "타이틀을 필수 항목입니다.")
    val title: String,
    @field:NotNull(message = "시작 날짜는 필수 항목입니다.")
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val start: LocalDateTime,
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val end: LocalDateTime?,
    val allDay: Boolean?,
    val note: String?,
    val isPublic: Boolean?,
    // 이메일
    val members: List<String> = listOf(),
) {
    @AssertTrue
    fun isStart(): Boolean {
        if (end == null) {
            return true
        }
        return end.isAfter(start)
    }
}
