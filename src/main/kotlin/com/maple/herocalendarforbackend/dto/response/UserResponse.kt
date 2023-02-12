package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.FriendshipStatusCode
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
    val world: String,
    val job: String,
    val jobDetail: String,
    val status: FriendshipStatusCode?,
) {
    companion object {
        fun convert(user: TUser, status: FriendshipStatusCode?) = UserResponse(
            id = user.id,
            email = user.email,
            nickName = user.nickName,
            accountId = user.accountId,
            avatarImg = user.avatarImg,
            isPublic = user.isPublic,
            world = user.world,
            job = user.job,
            jobDetail = user.jobDetail,
            status = status
        )
    }
}
