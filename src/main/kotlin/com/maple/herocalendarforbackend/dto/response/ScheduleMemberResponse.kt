package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class ScheduleMemberResponse(
    val id: String?,
    val accountId: String,
    val nickName: String,
    val avatarImg: String?,
    val acceptedStatus: AcceptedStatus,
) {
    companion object {
        fun convert(data: TScheduleMember) = ScheduleMemberResponse(
            id = data.groupKey.user.id,
            accountId = data.groupKey.user.accountId,
            nickName = data.groupKey.user.nickName,
            avatarImg = data.groupKey.user.avatarImg,
            acceptedStatus = data.acceptedStatus
        )
    }
}
