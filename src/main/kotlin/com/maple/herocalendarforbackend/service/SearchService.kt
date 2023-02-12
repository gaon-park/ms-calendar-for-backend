package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchService(
    private val userService: UserService,
    private val friendshipService: FriendshipService,
    private val scheduleService: ScheduleService,
) {
    fun findUser(loginUserId: String, request: SearchUserRequest): List<UserResponse> {
        val results =
            if (checkAllConditionIsNull(request)) userService.findByUpdatedAt(request.pageInfo)
            else userService.findByCondition(request)
        val friends = friendshipService.findByUserIdAndOppIn(loginUserId, results)
        val friendsId = friends.mapNotNull { it.id }

        return friends.plus(
            results.filter { !friendsId.contains(it.id) }
                .map {
                    UserResponse.convert(
                        it, null
                    )
                }
        )
    }

    private fun checkAllConditionIsNull(request: SearchUserRequest) =
        when {
            (!request.keyword.isNullOrEmpty()) -> false
            (!request.world.isNullOrEmpty()) -> false
            (!request.job.isNullOrEmpty()) -> false
            (!request.jobDetail.isNullOrEmpty()) -> false
            else -> true
        }

    fun findUserSchedules(
        loginUserId: String?, targetUserId: String, from: LocalDate, to: LocalDate
    ): List<ScheduleResponse> {
        loginUserId?.let {
            if (!friendshipService.friendCheck(loginUserId, targetUserId) &&
                !userService.findById(targetUserId).isPublic
            ) {
                return emptyList()
            }
        }
        return scheduleService.findForPublic(
            loginUserId = loginUserId,
            searchUserId = targetUserId,
            from = from,
            to = to
        )
    }
}
