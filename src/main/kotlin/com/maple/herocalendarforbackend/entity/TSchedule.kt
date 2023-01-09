package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.dto.request.ScheduleAddRequest
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
    val note: String?,
    val isPublic: Boolean,
    val waitingOwnerChange: Boolean,
    val ownerId: String?,
    val nextOwnerId: String?,
    @OneToMany(mappedBy = "schedule")
    var members: List<TScheduleMember> = listOf(),
) {
    companion object {
        fun convert(request: ScheduleAddRequest, ownerId: String?) = TSchedule(
            title = request.title,
            start = request.start,
            end = request.end,
            allDay = request.allDay ?: false,
            note = request.note ?: "",
            isPublic = request.isPublic ?: false,
            waitingOwnerChange = false,
            ownerId = ownerId,
            nextOwnerId = null
        )
    }
}
