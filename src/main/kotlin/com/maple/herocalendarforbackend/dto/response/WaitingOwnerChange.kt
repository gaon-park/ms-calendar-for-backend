package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TScheduleOwnerRequest
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class WaitingOwnerChange(
    val requester: UserResponse,
    val schedule: WaitingSchedule,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
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
