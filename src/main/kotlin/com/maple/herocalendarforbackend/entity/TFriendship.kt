package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_friendship")
data class TFriendship(
    @EmbeddedId
    val key: Key,
    val createdAt: LocalDateTime,
    val acceptedAt: LocalDateTime?
) {
    companion object {
        fun generateSaveModel(requester: TUser, respondent: TUser) = TFriendship(
            key = Key(requester, respondent),
            createdAt = LocalDateTime.now(),
            acceptedAt = null
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
