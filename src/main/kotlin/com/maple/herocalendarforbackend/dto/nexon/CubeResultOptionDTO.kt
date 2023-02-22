package com.maple.herocalendarforbackend.dto.nexon

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import lombok.Builder

@Builder
@JsonSerialize
data class CubeResultOptionDTO(
    @JsonProperty("value")
    val value: String,
    @JsonProperty("grade")
    val grade: String
)
