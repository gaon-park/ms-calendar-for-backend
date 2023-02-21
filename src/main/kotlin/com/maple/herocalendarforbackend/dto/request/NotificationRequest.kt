package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotNull
import lombok.Builder

@Builder
data class NotificationRequest(
    @field:NotNull
    val id: Long,
)
