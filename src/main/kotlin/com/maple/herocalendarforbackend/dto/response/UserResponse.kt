package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.FriendshipStatusCode
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class UserResponse(
    val id: String?,
    val nickName: String,
    val accountId: String,
    val avatarImg: String?,
    val isPublic: Boolean,
    val world: String,
    val job: String,
    val jobDetail: String,
    val holderFlg: Boolean,
    val status: FriendshipStatusCode?,
) {
    companion object {
        fun convert(user: TUser, status: FriendshipStatusCode?, holderFlg: Boolean) = UserResponse(
            id = user.id,
            nickName = user.nickName,
            accountId = user.accountId,
            avatarImg = user.avatarImg,
            isPublic = user.isPublic,
            world = user.world,
            job = user.job,
            jobDetail = user.jobDetail,
            holderFlg = holderFlg,
            status = status
        )
    }
}
