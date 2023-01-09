package com.maple.heroforbackend.service

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.entity.TFriendship
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.repository.TFriendshipRepository
import com.maple.heroforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FriendshipService(
    private val tUserRepository: TUserRepository,
    private val tFriendshipRepository: TFriendshipRepository,
    private val emailSendService: EmailSendService
) {

    fun findUserById(id: String): TUser {
        tUserRepository.findById(id).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            }
            return it.get()
        }
    }

    @Transactional
    fun friendRequest(requester: TUser, respondentId: String) {
        if (requester.id.equals(respondentId)) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        val respondent = findUserById(respondentId)
        tFriendshipRepository.findByKeyIn(
            listOf(
                TFriendship.Key(
                    requester = requester, respondent = respondent
                ), TFriendship.Key(
                    requester = respondent, respondent = requester
                )
            )
        )?.also { e ->
            // 요청자가 응답자에게 이미 친구 신청을 받은 상태인 경우
            if (e.key.requester.id == respondentId) {
                // 친구 관계를 수락
                tFriendshipRepository.save(e.copy(acceptedAt = LocalDateTime.now()))
            }
            // 이미 요청후, 응답 대기중인 경우
            else if (e.key.requester.id != respondentId && e.acceptedAt == null) {
                throw BaseException(BaseResponseCode.WAITING_FOR_RESPONDENT)
            }
        } ?: run {
            // 아무런 관계가 없음(이전에 요청했어도 거절당해서 초기화됨)
            // 요청
            emailSendService.sendFriendRequestEmail(requester, respondent.email)
            tFriendshipRepository.save(TFriendship.generateSaveModel(requester, respondent))
        }
    }

    @Transactional
    fun friendRequestAccept(requesterId: String, respondent: TUser) {
        val requester = findUserById(requesterId)
        tFriendshipRepository.findById(TFriendship.Key(requester, respondent)).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.BAD_REQUEST)
            }

            tFriendshipRepository.save(it.get().copy(acceptedAt = LocalDateTime.now()))
        }
    }

    @Transactional
    fun friendRequestRefuse(requesterId: String, respondent: TUser) {
        val requester = findUserById(requesterId)
        tFriendshipRepository.findById(TFriendship.Key(requester, respondent)).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.BAD_REQUEST)
            }

            tFriendshipRepository.delete(it.get())
        }
    }
}
