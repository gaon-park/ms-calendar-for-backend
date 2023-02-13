package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.FollowAcceptedStatus
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
@Table(name = "t_follow")
data class TFollow(
    @EmbeddedId
    val id: Key,
    @Enumerated(value = EnumType.STRING)
    val status: FollowAcceptedStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun generateSaveModel(requester: TUser, respondent: TUser) = TFollow(
            id = Key(requester, respondent),
            status = if (respondent.isPublic) FollowAcceptedStatus.ACCEPTED else FollowAcceptedStatus.WAITING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Embeddable
    data class Key(
        @ManyToOne
        @JoinColumn(name = "requester_id")
        val requester: TUser,
        @ManyToOne
        @JoinColumn(name = "respondent_id")
        val respondent: TUser,
    )
}
