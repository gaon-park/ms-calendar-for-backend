package com.maple.herocalendarforbackend.dto.request

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_VALUE_OF_MEMBERS
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ScheduleAddRequest(
    @field:NotEmpty(message = "타이틀을 필수 항목입니다.")
    val title: String,
    @field:NotNull(message = "시작 날짜는 필수 항목입니다.")
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val start: LocalDateTime,
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val end: LocalDateTime,
    @field:NotNull
    val repeat: Boolean,
    val repeatInfo: RepeatInfo?,
    val allDay: Boolean?,
    val note: String?,
    val isPublic: Boolean?,
    @field:Size(max = MAX_VALUE_OF_MEMBERS)
    val memberIds: List<String> = listOf(),
) {
    @AssertTrue
    fun isStart(): Boolean = (end.isAfter(start))

    @AssertTrue
    fun isRepeat(): Boolean {
        if (repeat) return repeatInfo != null
        return repeatInfo == null
    }
}