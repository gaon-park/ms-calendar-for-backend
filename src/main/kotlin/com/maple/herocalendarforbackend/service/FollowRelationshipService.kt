package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.follow.FollowRequest
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.entity.TFollowRelationship
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFollowRelationshipRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowRelationshipService(
    private val tUserRepository: TUserRepository,
    private val tFollowRelationshipRepository: TFollowRelationshipRepository,
) {

    fun followingCheck(userId: String, followingUserId: String): Boolean {
        return tFollowRelationshipRepository.followingCheck(
            requester = userId,
            respondent = followingUserId
        ) != null
    }

    fun findUserById(id: String): TUser {
        tUserRepository.findById(id).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            }
            return it.get()
        }
    }

    /**
     * 팔로우 요청 보내기
     */
    @Transactional
    fun followRequest(requesterId: String, request: FollowRequest) {
        val respondentId = request.personalKey
        if (requesterId == respondentId) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        val requester = findUserById(requesterId)
        val respondent = findUserById(respondentId)
        tFollowRelationshipRepository.findById(TFollowRelationship.Key(requester, respondent)).let {
            if (it.isEmpty) {
                tFollowRelationshipRepository.save(TFollowRelationship.generateSaveModel(requester, respondent))
            } else {
                val relationship = it.get()
                val exceptionCode = if (listOf(
                        AcceptedStatus.WAITING,
                        AcceptedStatus.REFUSED
                    ).contains(relationship.acceptedStatus)
                ) BaseResponseCode.WAITING_FOR_RESPONDENT
                else BaseResponseCode.ALREADY_FOLLOWING
                throw BaseException(exceptionCode)
            }
        }
    }

    /**
     * 팔로우/팔로우 요청 취소
     */
    @Transactional
    fun followCancel(requesterId: String, personalKey: String) {
        val requester = findUserById(requesterId)
        val respondent = findUserById(personalKey)
        tFollowRelationshipRepository.deleteById(TFollowRelationship.Key(requester, respondent))
    }

    /**
     * 팔로우 요청 수락
     */
    @Transactional
    fun followRequestAccept(opponentId: String, loginUserId: String) {
        tFollowRelationshipRepository.updateStatus(
            statusValue = AcceptedStatus.ACCEPTED.toString(),
            requesterId = opponentId,
            respondentId = loginUserId
        )
    }

    /**
     * 팔로우 요청 거절
     */
    @Transactional
    fun followRequestRefuse(opponentId: String, loginUserId: String) {
        tFollowRelationshipRepository.updateStatus(
            statusValue = AcceptedStatus.REFUSED.toString(),
            requesterId = opponentId,
            respondentId = loginUserId
        )
    }

    /**
     * 팔로잉 중인 유저의 리스트
     */
    fun findFollowings(userId: String): List<UserResponse> {
        return tFollowRelationshipRepository.findFollowingsByUserId(userId).map {
            UserResponse.convert(it.key.respondent, it.acceptedStatus)
        }
    }

    /**
     * 팔로워 리스트
     */
    fun findFollowers(userId: String): List<UserResponse> {
        return tFollowRelationshipRepository.findFollowersByUserId(userId).map {
            UserResponse.convert(it.key.requester, it.acceptedStatus)
        }
    }
}
