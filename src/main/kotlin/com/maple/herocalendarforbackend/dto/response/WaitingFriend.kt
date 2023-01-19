package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TFriendship
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class WaitingFriend(
    val requester: UserResponse,
    val note: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(data: TFriendship) = WaitingFriend(
            requester = UserResponse.convert(data.key.requester, AcceptedStatus.WAITING),
            note = data.note,
            createdAt = data.createdAt
        )
    }
}
