package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class RelationshipResponse(
    val id: String?,
    val nickName: String,
    val accountId: String,
    val acceptedStatus: AcceptedStatus
) {
    companion object {
        fun convert(user: TUser, acceptedStatus: AcceptedStatus) = RelationshipResponse(
            id = user.id,
            nickName = user.nickName,
            accountId = user.accountId,
            acceptedStatus = acceptedStatus
        )
    }
}
