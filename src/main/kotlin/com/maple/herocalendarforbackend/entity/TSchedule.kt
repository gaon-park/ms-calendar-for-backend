package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.RepeatCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
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
    val repeatStart: LocalDate,
    val repeatEnd: LocalDate,
    @Enumerated(value = EnumType.STRING)
    val repeatCode: RepeatCode?,
    val note: String?,
    val ownerId: String?,
) {
    companion object {
        fun convert(request: ScheduleAddRequest, ownerId: String?) = TSchedule(
            title = request.title,
            start = request.start,
            end = request.end,
            allDay = request.allDay ?: false,
            repeatStart = if (request.repeat) request.repeatInfo!!.start else request.start.toLocalDate(),
            repeatEnd = if (request.repeat) request.repeatInfo!!.end else request.end.toLocalDate(),
            repeatCode = if (request.repeat) request.repeatInfo!!.repeatCodeValue else null,
            note = request.note ?: "",
            ownerId = ownerId,
        )
    }
}
