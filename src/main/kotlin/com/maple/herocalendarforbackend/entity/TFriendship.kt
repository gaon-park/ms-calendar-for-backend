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
@Table(name = "t_friendship")
data class TFriendship(
    @EmbeddedId
    val key: Key,
    val note: String,
    @Enumerated(value = EnumType.STRING)
    val acceptedStatus: AcceptedStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun generateSaveModel(requester: TUser, respondent: TUser, note: String?) = TFriendship(
            key = Key(requester, respondent),
            note = note ?: "",
            acceptedStatus = AcceptedStatus.WAITING,
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
