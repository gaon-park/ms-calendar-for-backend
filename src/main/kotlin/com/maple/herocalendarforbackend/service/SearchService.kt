package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.SearchUserResponse
import com.maple.herocalendarforbackend.entity.IProfile
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchService(
    private val userService: UserService,
    private val followService: FollowService,
    private val scheduleService: ScheduleService,
) {
    fun findUserProfileByAccountId(accountId: String, loginUserId: String?): IProfileResponse? {
        return userService.findByAccountIdToIProfile(accountId, loginUserId)?.let {
            if (it.getIsPublic() || (it.getIFollowHim() != null && it.getIFollowHim() == "FOLLOW")) {
                IProfileResponse(
                    profile = it,
                    follow = followService.findFollows(it.getId()),
                    follower = followService.findFollowers(it.getId()),
                    acceptedFollowCount = followService.findCountJustAcceptedFollowByUserId(it.getId()),
                    acceptedFollowerCount = followService.findCountJustAcceptedFollowerByUserId(it.getId())
                )
            } else null
        }
    }

    fun findUser(request: SearchUserRequest, loginUserId: String?): SearchUserResponse {
        return SearchUserResponse.convert(
            users = userService.findByConditionAndUserId(request, loginUserId),
            fullHit = userService.findByConditionCount(request)
        )
    }

    fun findUserSchedules(
        loginUserId: String?, targetUserId: String, from: LocalDate, to: LocalDate
    ): List<ScheduleResponse> {
        return scheduleService.findForPublic(
            loginUserId = loginUserId,
            searchUserId = targetUserId,
            from = from,
            to = to
        )
    }
}
