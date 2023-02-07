package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.FollowAcceptedStatus
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class RelationshipResponse(
    val id: String?,
    val nickName: String,
    val accountId: String,
    val acceptedStatus: FollowAcceptedStatus
) {
    companion object {
        fun convert(user: TUser, acceptedStatus: FollowAcceptedStatus) = RelationshipResponse(
            id = user.id,
            nickName = user.nickName,
            accountId = user.accountId,
            acceptedStatus = acceptedStatus
        )
    }
}
