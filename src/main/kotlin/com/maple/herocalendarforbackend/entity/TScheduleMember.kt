package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.AcceptedStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "t_schedule_member")
data class TScheduleMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "schedule_id")
    val schedule: TSchedule,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: TUser,
    @Enumerated(value = EnumType.STRING)
    val acceptedStatus: AcceptedStatus
) {
    companion object {
        fun initConvert(user: TUser, schedule: TSchedule, acceptedStatus: AcceptedStatus) = TScheduleMember(
            schedule = schedule,
            user = user,
            acceptedStatus = acceptedStatus
        )
    }
}
