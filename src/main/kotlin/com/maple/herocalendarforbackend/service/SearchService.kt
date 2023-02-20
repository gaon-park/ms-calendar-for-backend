package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.dto.response.SimpleUserResponse
import com.maple.herocalendarforbackend.entity.IProfile
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val userService: UserService,
) {
    fun findUserProfileByAccountId(accountId: String, loginUserId: String?): IProfileResponse? {
        userService.findByAccountIdToIProfile(accountId, loginUserId)?.let {
            if (it.getIsPublic() || it.getIFollowHim() == "FOLLOW") {
                return userService.findByIdToIProfileResponse(it.getId())
            }
        }
        return null
    }

    fun findUser(request: SearchUserRequest, loginUserId: String?): List<IProfile> {
        return userService.findByConditionAndUserId(request, loginUserId)
    }

    fun findUserListForScheduleSearch(loginUserId: String?, keyword: String?): List<SimpleUserResponse> {
        return userService.findUserListForScheduleSearch(
            keyword, loginUserId
        ).map {
            SimpleUserResponse.convert(it)
        }
    }
}
