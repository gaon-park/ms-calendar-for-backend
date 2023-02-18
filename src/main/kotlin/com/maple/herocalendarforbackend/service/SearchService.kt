package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.entity.IProfile
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val userService: UserService,
    private val followService: FollowService,
) {
    fun findUserProfileByAccountId(accountId: String, loginUserId: String?): IProfileResponse? {
        return userService.findByAccountIdToIProfile(accountId, loginUserId)?.let {
            IProfileResponse(
                profile = it,
                follow = followService.findFollows(it.getId()),
                follower = followService.findFollowers(it.getId()),
                acceptedFollowCount = followService.findCountJustAcceptedFollowByUserId(it.getId()),
                acceptedFollowerCount = followService.findCountJustAcceptedFollowerByUserId(it.getId())
            )
        }
    }

    fun findUser(request: SearchUserRequest, loginUserId: String?): List<IProfile> {
        return userService.findByConditionAndUserId(request, loginUserId)
    }
}
