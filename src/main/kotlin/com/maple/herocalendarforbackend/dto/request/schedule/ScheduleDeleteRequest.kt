package com.maple.herocalendarforbackend.dto.request.schedule

import com.maple.herocalendarforbackend.code.ScheduleUpdateCode
import jakarta.validation.constraints.NotNull
import lombok.Builder

@Builder
data class ScheduleDeleteRequest(
    @field:NotNull
    val scheduleId: Long,
    val scheduleUpdateCode: ScheduleUpdateCode,
    val forOfficial: Boolean?
)
