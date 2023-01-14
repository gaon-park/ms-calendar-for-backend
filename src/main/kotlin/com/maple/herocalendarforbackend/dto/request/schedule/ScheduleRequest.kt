package com.maple.herocalendarforbackend.dto.request.schedule

import jakarta.validation.constraints.NotNull

data class ScheduleRequest(
    @field:NotNull
    val scheduleId: Long,
)
