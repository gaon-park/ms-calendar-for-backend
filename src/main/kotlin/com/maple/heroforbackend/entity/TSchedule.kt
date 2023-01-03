package com.maple.heroforbackend.entity

import com.maple.heroforbackend.dto.request.ScheduleAddRequest
import com.maple.heroforbackend.dto.request.ScheduleUpdateRequest
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_schedule")
data class TSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val allDay: Boolean,
    val note: String,
    val color: String?,
    val isPublic: Boolean,
    val waitingOwnerChange: Boolean,
    val nextOwnerId: Long?,
    @OneToMany(mappedBy = "schedule")
    var members: List<TScheduleMember> = listOf(),
) {
    companion object {
        fun convert(request: ScheduleAddRequest) = TSchedule(
            title = request.title,
            start = request.start,
            end = request.end,
            allDay = request.allDay ?: false,
            note = request.note ?: "",
            color = request.color,
            isPublic = request.isPublic ?: false,
            waitingOwnerChange = false,
            nextOwnerId = null
        )

        fun convert(id: Long, request: ScheduleUpdateRequest) = TSchedule(
            id = id,
            title = request.title,
            start = request.start,
            end = request.end,
            allDay = request.allDay ?: false,
            note = request.note ?: "",
            color = request.color,
            isPublic = request.isPublic ?: false,
            waitingOwnerChange = false,
            nextOwnerId = null
        )
    }
}
