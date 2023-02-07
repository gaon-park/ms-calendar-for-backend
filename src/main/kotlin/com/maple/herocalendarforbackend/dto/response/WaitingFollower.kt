package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.code.FollowAcceptedStatus
import com.maple.herocalendarforbackend.entity.TFollowRelationship
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class WaitingFollower(
    val requester: UserResponse,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="Asia/Seoul")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun convert(data: TFollowRelationship) = WaitingFollower(
            requester = UserResponse.convert(data.key.requester, FollowAcceptedStatus.WAITING),
            createdAt = data.createdAt
        )
    }
}
