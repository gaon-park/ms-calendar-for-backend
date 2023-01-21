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
data class TSchedule(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val allDay: Boolean,
    val ownerId: String?,
    @ManyToOne @JoinColumn(name = "member_group_id")
    val memberGroup: TScheduleMemberGroup,
    val isPublic: Boolean,
) {
    companion object {
        // 첫 입력
        fun convert(request: ScheduleAddRequest, ownerId: String?, memberGroup: TScheduleMemberGroup) = TSchedule(
            title = request.title,
            start = request.start,
            end = request.end,
            allDay = request.allDay ?: false,
            ownerId = ownerId,
            memberGroup = memberGroup,
            isPublic = request.isPublic
        )

        // 반복 입력
        fun convert(
            request: ScheduleAddRequest,
            ownerId: String?,
            memberGroup: TScheduleMemberGroup,
            start: LocalDateTime,
            end: LocalDateTime?
        ) = TSchedule(
            title = request.title,
            start = start,
            end = end,
            allDay = request.allDay ?: false,
            ownerId = ownerId,
            memberGroup = memberGroup,
            isPublic = request.isPublic
        )
    }
}
