package com.maple.heroforbackend.dto.request

import jakarta.validation.constraints.NotEmpty

data class ScheduleOwnerChangeRequest(
    @field:NotEmpty
    val nextOwnerEmail: String
)
