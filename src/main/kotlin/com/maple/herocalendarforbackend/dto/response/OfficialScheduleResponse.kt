package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TOfficialSchedule
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class OfficialScheduleResponse(
    val scheduleId: Long,
    val title: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val start: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val end: LocalDateTime?,
    val allDay: Boolean,
    val note: String?,
    val byAdmin: Boolean = true,
) {
    companion object {
        fun convert(data: TOfficialSchedule) = OfficialScheduleResponse(
            scheduleId = data.id!!,
            title = data.title,
            start = data.start,
            end = data.end,
            allDay = data.allDay,
            note = data.note
        )
    }
}
