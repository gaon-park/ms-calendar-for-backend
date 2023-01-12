package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TSchedule
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class ScheduleResponse(
    val scheduleId: Long,
    val title: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val start: LocalDateTime,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val end: LocalDateTime?,
    val allDay: Boolean,
    val url: String,
    val note: String?,
    val ownerId: String,
    val members: List<ScheduleMemberResponse>,
) {
    companion object {
        fun convert(data: TSchedule, members: List<ScheduleMemberResponse>) = ScheduleResponse(
            scheduleId = data.id!!,
            title = data.title,
            start = data.start,
            end = data.end,
            allDay = data.allDay,
            url = "", // todo
            note = data.note,
            ownerId = data.ownerId!!,
            members = members
        )
    }
}
