package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TScheduleMember
import lombok.Builder

@Builder
data class ScheduleMemberResponse(
    val email: String,
    val nickName: String,
    val acceptedStatus: AcceptedStatus,
) {
    companion object {
        fun convert(data: TScheduleMember) = ScheduleMemberResponse(
            email = data.scheduleKey.user.email,
            nickName = data.scheduleKey.user.nickName,
            acceptedStatus = data.acceptedStatus
        )
    }
}
