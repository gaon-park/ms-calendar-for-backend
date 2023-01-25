package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.exception.BaseException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
@Suppress("UnusedPrivateMember")
class SearchService(
    private val userService: UserService,
    private val friendshipService: FriendshipService,
    private val scheduleService: ScheduleService,
) {
    fun findFriendByEmailOrNickName(userId: String, searchUser: String): List<UserResponse> {
        val friends = friendshipService.findAllAcceptedStatusByUserId(userId)
            .map {
                UserResponse.convert(
                    if (it.key.requester.id == userId) it.key.respondent
                    else it.key.requester,
                    it.acceptedStatus
                )
            }.filter {
                it.nickName.contains(searchUser) ||
                        it.email.split("@")[0].contains(searchUser)
            }

        val friendIds = friends.mapNotNull { it.id }

        val users = findPublicByEmailOrNickName(searchUser)
            .filter { it.id != userId }
            .filter { !friendIds.contains(it.id) }
        return listOf<UserResponse>().plus(friends).plus(users)
    }

    fun findPublicByEmailOrNickName(user: String): List<UserResponse> =
        userService.findPublicByEmailOrNickName(user)
            .map {
                UserResponse.convert(it, null)
            }

    fun findFriendSchedules(
        userId: String, friendId: String, from: LocalDate?, to: LocalDate?
    ): List<ScheduleResponse> {
        if (!friendshipService.areTheyFriend(userId, friendId)) throw BaseException(BaseResponseCode.BAD_REQUEST)
        return scheduleService.findForPublic(friendId, from, to)
    }

    fun findPublicUserSchedules(
        searchUserId: String, from: LocalDate?, to: LocalDate?
    ): List<ScheduleResponse> {
        val user = userService.findById(searchUserId)
        if (!user.isPublic) throw BaseException(BaseResponseCode.BAD_REQUEST)
        return scheduleService.findForPublic(searchUserId, from, to)
    }
}
