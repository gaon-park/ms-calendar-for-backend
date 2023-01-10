package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.AcceptedStatus
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_schedule_member")
data class TScheduleMember(
    @EmbeddedId
    val scheduleKey: ScheduleKey,
    @Enumerated(value = EnumType.STRING)
    val acceptedStatus: AcceptedStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun initConvert(user: TUser, schedule: TSchedule, acceptedStatus: AcceptedStatus) = TScheduleMember(
            scheduleKey = ScheduleKey(schedule, user),
            acceptedStatus = acceptedStatus,
            createdAt = LocalDateTime.now()
        )
    }

    @Embeddable
    data class ScheduleKey(
        @ManyToOne
        @JoinColumn(name = "schedule_id")
        val schedule: TSchedule,
        @ManyToOne
        @JoinColumn(name = "user_id")
        val user: TUser
    )
}
