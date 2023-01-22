package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.AlertsResponse
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service

@Service
@Suppress("UnusedPrivateMember")
class AlertService(
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tFriendshipRepository: TFriendshipRepository,
    private val tUserRepository: TUserRepository,
) {
    fun findWaitingRequests(userId: String): AlertsResponse {
//        val scheduleInvites =
//            tScheduleMemberRepository.findByScheduleKeyUserIdAndAcceptedStatus(userId, AcceptedStatus.WAITING)
//        val owners =
//            tUserRepository.findByIdIn(scheduleInvites.mapNotNull { it.scheduleKey.schedule.ownerId })
//                .associateBy { it.id }
//        val ownerChangeRequests = tScheduleOwnerRequestRepository.findByRespondentId(userId)
//        val friendRequests =
//            tFriendshipRepository.findByKeyRespondentIdAndAcceptedStatus(userId, AcceptedStatus.WAITING)
//
//        return AlertsResponse(
//            ownerChangesRequests = ownerChangeRequests.map {
//                WaitingOwnerChange.convert(it)
//            },
//            waitingScheduleRequests = scheduleInvites.map {
//                WaitingSchedule.convert(it, owners[it.scheduleKey.schedule.ownerId]!!)
//            },
//            waitingFriendRequests = friendRequests.map {
//                WaitingFriend.convert(it)
//            }
//        )
        return AlertsResponse(
            emptyList()
        )
    }
}
