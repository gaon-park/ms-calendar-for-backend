package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.FriendshipAcceptStatusCode
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
    @Enumerated(value = EnumType.STRING)
    val status: FriendshipAcceptStatusCode,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun generateSaveModel(requester: TUser, respondent: TUser) = TFriendship(
            key = Key(requester, respondent),
            status = if (respondent.isPublic) FriendshipAcceptStatusCode.ACCEPTED
            else FriendshipAcceptStatusCode.WAITING,
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
