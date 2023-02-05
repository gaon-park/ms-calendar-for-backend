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
    val ownerId: String?,
    @ManyToOne @JoinColumn(name = "member_group_id")
    val memberGroup: TScheduleMemberGroup,
    val isPublic: Boolean,
    val parentId: Long?,
) {
    companion object {
        // 첫 입력
        fun convert(request: ScheduleAddRequest, ownerId: String?, memberGroup: TScheduleMemberGroup) = TSchedule(
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
            ownerId = ownerId,
            memberGroup = memberGroup,
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
            ownerId = schedule.ownerId,
            memberGroup = schedule.memberGroup,
            isPublic = request.isPublic,
            parentId = schedule.id
        )
    }
}
