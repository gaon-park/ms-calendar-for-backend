package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotNull

data class ScheduleRequest(
    @field:NotNull
    val scheduleId: Long,
)
