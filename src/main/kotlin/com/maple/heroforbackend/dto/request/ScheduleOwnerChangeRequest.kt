package com.maple.heroforbackend.dto.request

import jakarta.validation.constraints.NotNull

data class ScheduleOwnerChangeRequest(
    @NotNull
    val nextOwnerEmail: String
)
