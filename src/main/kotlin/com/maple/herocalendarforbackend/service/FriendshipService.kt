package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.entity.TFriendship
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
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

    /**
     * 친구 요청 보내기
     */
    @Transactional
    fun friendRequest(requesterId: String, request: FriendAddRequest) {
        val respondentId = request.personalKey
        if (requesterId == respondentId) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        val requester = findUserById(requesterId)
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
            // 요청자가 응답자에게 이미 친구 신청을 받은 상태인 경우(이전에 거절한 것도 포함)
            if (e.key.requester.id == respondentId && e.acceptedStatus != AcceptedStatus.ACCEPTED) {
                // 친구 관계를 수락
                tFriendshipRepository.save(
                    e.copy(
                        acceptedStatus = AcceptedStatus.ACCEPTED,
                        updatedAt = LocalDateTime.now()
                    )
                )
            }
            // 이미 요청후, 응답 대기중/거절 당한 경우 (애써 대기중이라 말해준다)
            else if (e.key.requester.id != respondentId && listOf(
                    AcceptedStatus.WAITING,
                    AcceptedStatus.REFUSED
                ).contains(e.acceptedStatus)
            ) {
                throw BaseException(BaseResponseCode.WAITING_FOR_RESPONDENT)
            }
        } ?: run {
            // 아무런 관계가 없음
            // 요청
            emailSendService.sendFriendRequestEmail(requester, respondent.email)
            tFriendshipRepository.save(TFriendship.generateSaveModel(requester, respondent, request.note))
        }
    }

    /**
     * 친구 요청 수락
     */
    @Transactional
    fun friendRequestAccept(opponentId: String, loginUserId: String) {
        val requester = findUserById(opponentId)
        val respondent = findUserById(loginUserId)
        tFriendshipRepository.findById(TFriendship.Key(requester, respondent)).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.BAD_REQUEST)
            }

            val entity = it.get()
            if (entity.acceptedStatus != AcceptedStatus.ACCEPTED) {
                tFriendshipRepository.save(
                    it.get().copy(acceptedStatus = AcceptedStatus.ACCEPTED, updatedAt = LocalDateTime.now())
                )
            }
        }
    }

    /**
     * 친구 요청 거절
     */
    @Transactional
    fun friendRequestRefuse(opponentId: String, loginUserId: String) {
        val requester = findUserById(opponentId)
        val respondent = findUserById(loginUserId)
        tFriendshipRepository.findById(TFriendship.Key(requester, respondent)).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.BAD_REQUEST)
            }

            val entity = it.get()
            if (entity.acceptedStatus != AcceptedStatus.REFUSED) {
                tFriendshipRepository.save(
                    it.get().copy(acceptedStatus = AcceptedStatus.REFUSED, updatedAt = LocalDateTime.now())
                )
            }
        }
    }
}
