package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_schedule")
@Suppress("MagicNumber")
data class TSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: TUser,
    @ManyToOne @JoinColumn(name = "member_group_id")
    val memberGroup: TScheduleMemberGroup,
    @ManyToOne @JoinColumn(name = "note_id")
    val note: TScheduleNote?,
    val isPublic: Boolean,
    val parentId: Long?,
) {
    companion object {
        // 첫 입력
        fun convert(
            request: ScheduleAddRequest,
            owner: TUser,
            memberGroup: TScheduleMemberGroup,
            note: TScheduleNote?
        ) = TSchedule(
            title = request.title,
            start = request.start,
            end = if (request.allDay == true) LocalDateTime.of(
                request.start.year,
                request.start.month,
                request.start.dayOfMonth,
                23,
                59
            )
            else request.end ?: request.start,
            allDay = request.allDay ?: false,
            owner = owner,
            memberGroup = memberGroup,
            note = note,
            isPublic = request.isPublic,
            parentId = null
        )

        // 반복 입력
        fun convert(
            request: ScheduleAddRequest,
            schedule: TSchedule,
            start: LocalDateTime,
            end: LocalDateTime
        ) = TSchedule(
            title = request.title,
            start = start,
            end = end,
            allDay = request.allDay ?: false,
            owner = schedule.owner,
            memberGroup = schedule.memberGroup,
            note = schedule.note,
            isPublic = request.isPublic,
            parentId = schedule.id
        )
    }
}
