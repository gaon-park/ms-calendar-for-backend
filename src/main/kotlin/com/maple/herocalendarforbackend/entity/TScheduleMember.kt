package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.AcceptedStatus
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "t_schedule_member")
data class TScheduleMember(
    @EmbeddedId
    val scheduleKey: ScheduleKey,
    @Enumerated(value = EnumType.STRING)
    val acceptedStatus: AcceptedStatus
) {
    companion object {
        fun initConvert(user: TUser, schedule: TSchedule, acceptedStatus: AcceptedStatus) = TScheduleMember(
            scheduleKey = ScheduleKey(schedule, user),
            acceptedStatus = acceptedStatus
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
