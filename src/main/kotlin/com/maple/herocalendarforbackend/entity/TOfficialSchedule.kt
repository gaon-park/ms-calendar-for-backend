package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.exception.BaseException
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_official_schedule")
data class TOfficialSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val note: String,
) {
    companion object {
        fun convert(request: ScheduleAddRequest) = TOfficialSchedule(
            title = request.title,
            start = request.start,
            end = request.end ?: throw BaseException(BaseResponseCode.BAD_REQUEST),
            allDay = request.allDay ?: false,
            note = request.note ?: "",
        )
    }
}
