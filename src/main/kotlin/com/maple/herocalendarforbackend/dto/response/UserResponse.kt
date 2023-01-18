package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class UserResponse(
    val id: String?,
    val email: String,
    val nickName: String,
    val avatarImg: String,
) {
    companion object {
        fun convert(user: TUser) = UserResponse(
            id = user.id,
            email = user.email,
            nickName = user.nickName,
            avatarImg = user.avatarImg
        )

        fun convert(list: List<TUser>): List<UserResponse> =
            list.map {
                convert(it)
            }
    }
}
