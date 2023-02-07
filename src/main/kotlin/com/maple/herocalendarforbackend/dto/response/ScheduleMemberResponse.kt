package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.ScheduleAcceptedStatus
import com.maple.herocalendarforbackend.entity.TScheduleMember
import lombok.Builder

@Builder
data class ScheduleMemberResponse(
    val id: String?,
    val email: String,
    val nickName: String,
    val acceptedStatus: ScheduleAcceptedStatus,
) {
    companion object {
        fun convert(data: TScheduleMember) = ScheduleMemberResponse(
            id = data.groupKey.user.id,
            email = data.groupKey.user.email,
            nickName = data.groupKey.user.nickName,
            acceptedStatus = data.acceptedStatus
        )
    }
}
