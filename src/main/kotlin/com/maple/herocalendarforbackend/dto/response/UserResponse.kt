package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class UserResponse(
    val id: String?,
    val email: String,
    val nickName: String,
    val avatarImg: String,
    val acceptedStatus: AcceptedStatus?,
) {
    companion object {
        fun convert(user: TUser, acceptedStatus: AcceptedStatus?) = UserResponse(
            id = user.id,
            email = user.email,
            nickName = user.nickName,
            avatarImg = user.avatarImg,
            acceptedStatus = acceptedStatus
        )
    }
}
