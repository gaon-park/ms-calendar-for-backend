package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class SimpleUserResponse(
    val id: String?,
    val accountId: String,
    val nickName: String,
    val avatarImg: String?
) {
    companion object {
        fun convert(user: TUser) = SimpleUserResponse(
            id = user.id,
            accountId = user.accountId,
            nickName = user.nickName,
            avatarImg = user.avatarImg
        )
    }
}
