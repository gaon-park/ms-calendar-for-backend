package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.ProfileInterface
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class WaitingFollower(
    val requester: ProfileInterface,
    val createdAt: LocalDateTime,
)
