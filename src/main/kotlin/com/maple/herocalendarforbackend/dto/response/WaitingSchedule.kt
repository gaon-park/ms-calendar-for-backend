package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.entity.TScheduleOwnerRequest
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class WaitingSchedule(
    val id: Long,
    val title: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val start: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val end: LocalDateTime?,
    val allDay: Boolean,
    val owner: UserResponse,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val createdAt: LocalDateTime
) {
    companion object {
        fun convert(data: TScheduleMember, owner: TUser): WaitingSchedule {
            val schedule = data.scheduleKey.schedule
            return WaitingSchedule(
                id = schedule.id!!,
                title = schedule.title,
                start = schedule.start,
                end = schedule.end,
                allDay = schedule.allDay,
                owner = UserResponse.convert(owner, AcceptedStatus.WAITING),
                createdAt = data.createdAt
            )
        }

        fun convert(data: TScheduleOwnerRequest): WaitingSchedule {
            val schedule = data.requestId.schedule
            val owner = data.requestId.owner
            return WaitingSchedule(
                id = schedule.id!!,
                title = schedule.title,
                start = schedule.start,
                end = schedule.end,
                allDay = schedule.allDay,
                owner = UserResponse.convert(owner, AcceptedStatus.WAITING),
                createdAt = data.createdAt
            )
        }
    }
}
