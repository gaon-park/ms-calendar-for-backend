package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotNull

data class ScheduleMemberAddRequest(
    @field:NotNull
    val scheduleId: Long,
    val newMember: List<String> = listOf()
)
