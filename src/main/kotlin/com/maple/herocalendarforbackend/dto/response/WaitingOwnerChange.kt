package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TScheduleOwnerRequest
import java.time.LocalDateTime

data class WaitingOwnerChange(
    val requester: UserResponse,
    val schedule: WaitingSchedule,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(data: TScheduleOwnerRequest) : WaitingOwnerChange {
            val owner = data.requestId.owner
            return WaitingOwnerChange(
                requester = UserResponse(owner.id, owner.email, owner.nickName),
                schedule = WaitingSchedule.convert(data),
                createdAt = data.createdAt
            )
        }
    }
}
