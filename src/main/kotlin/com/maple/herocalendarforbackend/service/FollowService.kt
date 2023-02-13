package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.FollowAcceptedStatus
import com.maple.herocalendarforbackend.dto.response.ProfileResponse
import com.maple.herocalendarforbackend.entity.ProfileInterface
import com.maple.herocalendarforbackend.entity.TFollow
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFollowRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val tUserRepository: TUserRepository,
    private val tFollowRepository: TFollowRepository,
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
        tFollowRepository.findById(loginUserId, respondentId) ?: tFollowRepository.save(
            TFollow.generateSaveModel(findUserById(loginUserId), findUserById(respondentId))
        )
    }

    @Transactional
    fun followCancel(loginUserId: String, respondentId: String) {
        tFollowRepository.deleteById(loginUserId, respondentId)
    }

    @Transactional
    fun deleteFromMyFollower(loginUserId: String, followerId: String) {
        tFollowRepository.deleteById(followerId, loginUserId)
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
        }
    }

    fun findFollows(userId: String): List<ProfileInterface> {
        return tFollowRepository.findFollowByUserId(userId)
    }

    fun findFollowers(userId: String): List<ProfileInterface> {
        return tFollowRepository.findFollowerByUserId(userId)
    }
}
