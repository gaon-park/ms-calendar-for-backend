package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TFriendship
import java.time.LocalDateTime

data class WaitingFriendRequest(
    val requester: UserResponse,
    val note: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(data: TFriendship) = WaitingFriendRequest(
            requester = UserResponse(
                email = data.key.requester.email,
                nickName = data.key.requester.nickName
            ),
            note = data.note,
            createdAt = data.createdAt
        )
    }
}
