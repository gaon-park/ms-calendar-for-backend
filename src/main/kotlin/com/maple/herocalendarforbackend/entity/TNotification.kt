package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Suppress("LongParameterList")
@Entity
@Table(name = "t_notification")
data class TNotification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    val subTitle: String,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: TUser,
    val createdAt: LocalDateTime,
    val newFollowId: String?,
    val newFollowerId: String?,
    val newScheduleRequesterId: String?,
    val scheduleRespondentId: String?
) {
    companion object {
        fun generate(
            title: String,
            subTitle: String,
            user: TUser,
            newFollowId: String?,
            newFollowerId: String?,
            newScheduleRequesterId: String?,
            scheduleRespondentId: String?
        ) = TNotification(
            title = title,
            subTitle = subTitle,
            user = user,
            createdAt = LocalDateTime.now(),
            newFollowId = newFollowId,
            newFollowerId = newFollowerId,
            newScheduleRequesterId = newScheduleRequesterId,
            scheduleRespondentId = scheduleRespondentId,
        )
    }
}
