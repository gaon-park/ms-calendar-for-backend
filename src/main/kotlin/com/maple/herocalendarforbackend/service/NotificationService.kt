package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.NotificationResponse
import com.maple.herocalendarforbackend.repository.TNotificationRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class NotificationService(
    private val tUserRepository: TUserRepository,
    private val tNotificationRepository: TNotificationRepository
) {

    fun findAll(loginUserId: String): List<NotificationResponse> {
        val now = LocalDateTime.now()
        val notifications = tNotificationRepository.findByUserId(loginUserId)
        if (notifications.isEmpty()) return emptyList()

        val notificationOppUsers = notifications.associate {
            it.id to when {
                it.newFollowId !== null -> it.newFollowId
                it.newFollowerId !== null -> it.newFollowerId
                it.newScheduleRequesterId !== null -> it.newScheduleRequesterId
                it.scheduleRespondentId !== null -> it.scheduleRespondentId
                else -> null
            }
        }
        val userMap = tUserRepository.findByIdIn(notificationOppUsers.values.mapNotNull { it }.toList())
            .associateBy { it.id }

        val n = notifications.mapNotNull {
            userMap[notificationOppUsers[it.id]]?.let { user ->
                NotificationResponse(
                    notificationId = it.id,
                    meta = getMeta(it.createdAt, now),
                    title = it.title,
                    subTitle = it.subTitle,
                    avatarImg = user.avatarImg,
                    avatarText = user.nickName,
                    linkPath = when {
                        it.newFollowId !== null -> "/user-profile/follow"
                        it.newFollowerId !== null -> "/user-profile/follower"
                        else -> "/apps/calendar"
                    }
                )
            }
        }

        return n
    }

    @Transactional
    fun deleteByReadAllEvent(loginUserId: String) {
        tNotificationRepository.deleteByReadAllEvent(loginUserId)
    }

    @Transactional
    fun deleteByRead(loginUserId: String, notificationId: Long) {
        tNotificationRepository.deleteByRead(loginUserId, notificationId)
    }

    private fun getMeta(createdAt: LocalDateTime, now: LocalDateTime): String {
        val year = ChronoUnit.YEARS.between(createdAt, now)
        val month = ChronoUnit.MONTHS.between(createdAt, now)
        val day = ChronoUnit.DAYS.between(createdAt, now)
        val hour = ChronoUnit.HOURS.between(createdAt, now)
        val min = ChronoUnit.MINUTES.between(createdAt, now)
        return when {
            (year > 0 || month > 0 || day > 2) -> createdAt.toLocalDate().toString()
            (day == 2L) -> "그저께"
            (day == 1L) -> "어제"
            (hour > 0) -> "${hour}시간 전"
            (min > 0) -> "${min}분 전"
            else -> "방금"
        }
    }
}
