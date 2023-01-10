package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TScheduleMember
import java.time.LocalDateTime

data class WaitingScheduleRequest(
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val allDay: Boolean,
    val owner: UserResponse,
    val createdAt: LocalDateTime
) {
    companion object {
        fun convert(data: TScheduleMember): WaitingScheduleRequest {
            val schedule = data.scheduleKey.schedule
            val user = schedule.members.first {
                it.scheduleKey.user.id == schedule.ownerId
            }.scheduleKey.user
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
