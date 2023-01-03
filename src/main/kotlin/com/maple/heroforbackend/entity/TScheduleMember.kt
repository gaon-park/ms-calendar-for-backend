package com.maple.heroforbackend.entity

import jakarta.persistence.Entity
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
    val isOwner: Boolean,
    val accepted: Boolean,
) {
    companion object {
        fun initConvert(user: TUser, isOwner: Boolean, schedule: TSchedule) = TScheduleMember(
            schedule = schedule,
            user = user,
            isOwner = isOwner,
            accepted = isOwner
        )
    }
}
