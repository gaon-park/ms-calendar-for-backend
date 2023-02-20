package com.maple.herocalendarforbackend.dto.response

import lombok.Builder

@Builder
data class ScheduleResponseTMP(
    val officials: List<OfficialScheduleResponse>,
    val personals: List<PersonalScheduleResponse>
)
