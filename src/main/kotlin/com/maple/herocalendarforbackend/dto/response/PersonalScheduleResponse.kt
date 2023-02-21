package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TSchedule
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class PersonalScheduleResponse(
    val scheduleId: Long,
    val title: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val start: LocalDateTime,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val end: LocalDateTime?,
    val allDay: Boolean,
    val note: String?,
    val owner: SimpleUserResponse,
    val isPublic: Boolean,
    val members: List<ScheduleMemberResponse>,
) {
    companion object {
        fun convert(data: TSchedule, members: List<ScheduleMemberResponse>, note: String?) = PersonalScheduleResponse(
            scheduleId = data.id!!,
            title = data.title,
            start = data.start,
            end = data.end,
            allDay = data.allDay,
            isPublic = data.isPublic,
            note = note,
            owner = SimpleUserResponse.convert(data.owner),
            members = members
        )
    }
}
