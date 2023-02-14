package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.IProfile
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class WaitingFollower(
    val requester: IProfile,
    val createdAt: LocalDateTime,
)
