package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class ScheduleOwnerChangeRequest(
    @field:NotNull
    val scheduleId: Long,
    @field:NotEmpty
    val nextOwnerEmail: String
)
