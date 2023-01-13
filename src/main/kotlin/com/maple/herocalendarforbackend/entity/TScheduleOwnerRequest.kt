package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.AcceptedStatus
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
@Table(name = "t_schedule_owner_request")
data class TScheduleOwnerRequest(
    @EmbeddedId
    val requestId: OwnerRequestId,
    @ManyToOne
    @JoinColumn(name = "respondent_id")
    val respondent: TUser,
    @Enumerated(value = EnumType.STRING)
    val acceptedStatus: AcceptedStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(schedule: TSchedule, owner: TUser, respondent: TUser) = TScheduleOwnerRequest(
            OwnerRequestId(
                schedule = schedule,
                owner = owner,
            ),
            respondent = respondent,
            acceptedStatus = AcceptedStatus.WAITING,
            createdAt = LocalDateTime.now()
        )
    }

    @Embeddable
    data class OwnerRequestId(
        @ManyToOne
        @JoinColumn(name = "schedule_id")
        val schedule: TSchedule,
        @ManyToOne
        @JoinColumn(name = "requester_id")
        val owner: TUser
    )
}
