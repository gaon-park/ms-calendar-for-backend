package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TScheduleMember
import java.time.LocalDateTime

data class WaitingScheduleRequest(
    val title: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val start: LocalDateTime,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val end: LocalDateTime?,
    val allDay: Boolean,
    val owner: UserResponse,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val createdAt: LocalDateTime
) {
    companion object {
        fun convert(data: TScheduleMember): WaitingScheduleRequest {
            val schedule = data.scheduleKey.schedule
            return WaitingScheduleRequest(
                title = schedule.title,
                start = schedule.start,
                end = schedule.end,
                allDay = schedule.allDay,
                owner = UserResponse(
                    email = "",
                    nickName = ""
                ),
                createdAt = data.createdAt
            )
        }
    }
}
