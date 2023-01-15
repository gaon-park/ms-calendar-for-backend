package com.maple.herocalendarforbackend.dto.request.schedule

import jakarta.validation.constraints.NotNull
import lombok.Builder

@Builder
data class ScheduleRequest(
    @field:NotNull
    val scheduleId: Long,
)
