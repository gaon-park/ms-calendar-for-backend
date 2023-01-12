package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TFriendship
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

data class WaitingFriendRequest(
    val requester: UserResponse,
    val note: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
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
