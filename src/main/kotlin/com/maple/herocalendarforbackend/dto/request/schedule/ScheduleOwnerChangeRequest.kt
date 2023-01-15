package com.maple.herocalendarforbackend.dto.request.schedule

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import lombok.Builder

@Builder
data class ScheduleOwnerChangeRequest(
    @field:NotNull
    val scheduleId: Long,
    @field:NotEmpty
    val nextOwnerId: String
)
