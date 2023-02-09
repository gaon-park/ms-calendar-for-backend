package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_VALUE_OF_MEMBERS
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchService(
    private val userService: UserService,
    private val followRelationshipService: FollowRelationshipService,
    private val scheduleService: ScheduleService,
) {
    fun findUser(searchWord: String): List<UserResponse> {
        return userService.findByKeywordLike(searchWord, null).map {
            UserResponse.convert(it, null)
        }
    }

    fun findUser(searchWord: String, loginUserId: String): List<UserResponse> {
        val followers = followRelationshipService.findFollowings(loginUserId).filter {
            it.accountId.contains(searchWord)
        }
        val followerIds = followers.map { it.id }
        var searchResult = emptyList<UserResponse>();
        if (followers.size < MAX_VALUE_OF_MEMBERS) {
            searchResult = userService.findByKeywordLike(searchWord, loginUserId).map {
                UserResponse.convert(it, null)
            }.filter { !followerIds.contains(it.id) }
        }
        return listOf(followers, searchResult).flatten()
    }

    fun findUserSchedules(
        loginUserId: String?, targetUserId: String, from: LocalDate, to: LocalDate
    ): List<ScheduleResponse> {
        loginUserId?.let {
            if (!followRelationshipService.followingCheck(loginUserId, targetUserId) &&
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
