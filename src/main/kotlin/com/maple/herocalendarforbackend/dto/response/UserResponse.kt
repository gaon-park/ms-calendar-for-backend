package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.FollowAcceptedStatus
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class UserResponse(
    val id: String?,
    val email: String,
    val nickName: String,
    val accountId: String,
    val avatarImg: String?,
    val isPublic: Boolean,
    val acceptedStatus: FollowAcceptedStatus?,
) {
    companion object {
        fun convert(user: TUser, acceptedStatus: FollowAcceptedStatus?) = UserResponse(
            id = user.id,
            email = user.email,
            nickName = user.nickName,
            accountId = user.accountId,
            avatarImg = user.avatarImg,
            isPublic = user.isPublic,
            acceptedStatus = acceptedStatus
        )
    }
}
