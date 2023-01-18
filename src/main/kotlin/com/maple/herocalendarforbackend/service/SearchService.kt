package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchService(
    private val userService: UserService,
    private val friendshipService: FriendshipService,
    private val scheduleService: ScheduleService,
) {
    fun findFriendByEmailOrNickName(userId: String, searchUser: String): List<TUser> {
        val friends = friendshipService.findAcceptedFriend(userId)
            .map {
                if (it.key.requester.id == userId) it.key.respondent
                else it.key.requester
            }.filter {
                it.nickName.contains(searchUser) ||
                        it.email.contains(searchUser)
            }

        val users = findPublicByEmailOrNickName(searchUser)
            .filter { it.id != userId }
        return listOf<TUser>().plus(friends).plus(users).toSet().toList()
    }

    fun findPublicByEmailOrNickName(user: String): List<TUser> =
        userService.findPublicByEmailOrNickName(user)

    fun findFriendSchedulesAndConvertToResponse(
        userId: String, friendId: String, from: LocalDate?, to: LocalDate?
    ): List<ScheduleResponse> {
        if (!friendshipService.areTheyFriend(userId, friendId)) throw BaseException(BaseResponseCode.BAD_REQUEST)
        return scheduleService.findSchedulesAndConvertToResponse(friendId, from, to)
    }

    fun findPublicUserSchedulesAndConvertToResponse(
        searchUserId: String, from: LocalDate?, to: LocalDate?
    ): List<ScheduleResponse> {
        val user = userService.findById(searchUserId)
        if (!user.isPublic) throw BaseException(BaseResponseCode.BAD_REQUEST)
        return scheduleService.findSchedulesAndConvertToResponse(searchUserId, from, to)
    }
}
