package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class ScheduleResponse(
    val officials: List<OfficialScheduleResponse>,
    val personals: Map<String, List<PersonalScheduleResponse>>
)
