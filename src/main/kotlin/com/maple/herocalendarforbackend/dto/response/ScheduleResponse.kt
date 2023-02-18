package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.entity.TSchedule
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class ScheduleResponse(
    val officials: List<OfficialScheduleResponse>,
    val personals: Map<String, List<PersonalScheduleResponse>>
)
