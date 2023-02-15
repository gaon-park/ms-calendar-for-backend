package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.FollowAcceptedStatus
import com.maple.herocalendarforbackend.entity.IProfile
import com.maple.herocalendarforbackend.entity.TFollow
import com.maple.herocalendarforbackend.entity.TNotification
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFollowRepository
import com.maple.herocalendarforbackend.repository.TNotificationRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val tUserRepository: TUserRepository,
    private val tFollowRepository: TFollowRepository,
    private val tNotificationRepository: TNotificationRepository
) {

    private fun findUserById(id: String): TUser {
        tUserRepository.findById(id).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            }
            return it.get()
        }
    }

    @Transactional
    fun followRequest(loginUserId: String, respondentId: String) {
        if (tFollowRepository.findById(loginUserId, respondentId) == null) {
            val requester = findUserById(loginUserId)
            val respondent = findUserById(respondentId)
            tFollowRepository.save(TFollow.generateSaveModel(requester, respondent))

            if (respondent.notificationFlg) {
                tNotificationRepository.save(
                    TNotification.generate(
                        title = requester.accountId,
                        subTitle = if (respondent.isPublic) "당신을 팔로우하기 시작했어요!"
                        else "@${requester.accountId} 님이 팔로우 하고 싶어해요",
                        user = respondent,
                        newFollowId = null,
                        newFollowerId = requester.id,
                        newScheduleRequesterId = null,
                        scheduleRespondentId = null,
                    )
                )
            }
        }
    }

    @Transactional
    fun followCancel(loginUserId: String, respondentId: String) {
        tFollowRepository.deleteById(loginUserId, respondentId)
        tNotificationRepository.deleteByFollowCancel(respondentId, loginUserId)
    }

    @Transactional
    fun deleteFromMyFollower(loginUserId: String, followerId: String) {
        tFollowRepository.deleteById(followerId, loginUserId)
        tNotificationRepository.deleteByFollowerDelete(loginUserId, followerId)
    }

    @Transactional
    fun requestAccept(loginUserId: String, followerId: String) {
        val relation = tFollowRepository.findById(followerId, loginUserId)
        if (relation != null && relation.status != FollowAcceptedStatus.ACCEPTED) {
            tFollowRepository.save(
                relation.copy(
                    status = FollowAcceptedStatus.ACCEPTED
                )
            )
            tNotificationRepository.save(
                TNotification.generate(
                    title = relation.id.respondent.accountId,
                    subTitle = "팔로우 요청을 수락했어요!",
                    user = relation.id.requester,
                    newFollowId = relation.id.respondent.id,
                    newFollowerId = null,
                    newScheduleRequesterId = null,
                    scheduleRespondentId = null
                )
            )
        }
    }

    fun findFollows(userId: String): List<IProfile> {
        return tFollowRepository.findAllStatusFollowByUserId(userId)
    }

    fun findFollowers(userId: String): List<IProfile> {
        return tFollowRepository.findAllStatusFollowerByUserId(userId)
    }

    fun findCountJustAcceptedFollowByUserId(userId: String): Long {
        return tFollowRepository.findCountJustAcceptedFollowByUserId(userId)
    }

    fun findCountJustAcceptedFollowerByUserId(userId: String): Long {
        return tFollowRepository.findCountJustAcceptedFollowerByUserId(userId)
    }
}
