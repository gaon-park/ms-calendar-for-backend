package com.maple.herocalendarforbackend.dto.request.follow

import jakarta.validation.constraints.NotEmpty
import lombok.Builder

@Builder
data class FollowRequest(
    @field:NotEmpty
    val personalKey: String,
)
