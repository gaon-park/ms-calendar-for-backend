package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.SearchUserResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchService(
    private val userService: UserService,
    private val scheduleService: ScheduleService,
) {
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
