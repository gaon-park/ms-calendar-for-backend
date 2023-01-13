package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TFriendship
import java.time.LocalDateTime

data class WaitingFriend(
    val requester: UserResponse,
    val note: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(data: TFriendship) = WaitingFriend(
            requester = UserResponse(
                id = data.key.requester.id,
                email = data.key.requester.email,
                nickName = data.key.requester.nickName
            ),
            note = data.note,
            createdAt = data.createdAt
        )
    }
}
