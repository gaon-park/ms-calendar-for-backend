package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import org.springframework.stereotype.Service

@Service
class AlertService(
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tFriendshipRepository: TFriendshipRepository,
) {

    /**
     * 유저의 미응답 스케줄 요청 검색
     */
    fun findWaitingScheduleRequests(userId: String) =
        tScheduleMemberRepository.findByScheduleKeyUserIdAndAcceptedStatus(userId, AcceptedStatus.WAITING)

    /**
     * 유저의 미응답 친구 요청 검색
     */
    fun findWaitingFriendRequests(userId: String) =
        tFriendshipRepository.findByKeyRespondentIdAndAcceptedStatus(userId, AcceptedStatus.WAITING)
}
