package com.maple.herocalendarforbackend.dto.request.schedule

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_VALUE_OF_MEMBERS
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.Builder

@Builder
data class ScheduleMemberAddRequest(
    @field:NotNull
    val scheduleId: Long,
    @field:Size(max = MAX_VALUE_OF_MEMBERS)
    val newMemberIds: List<String> = listOf()
)
