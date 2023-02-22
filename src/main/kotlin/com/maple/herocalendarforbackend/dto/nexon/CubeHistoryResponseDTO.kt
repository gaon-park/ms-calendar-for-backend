package com.maple.herocalendarforbackend.dto.nexon

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import lombok.Builder

@Builder
@JsonSerialize
data class CubeHistoryResponseDTO(
    @JsonProperty("count")
    val count: Int?,
    @JsonProperty("cube_histories")
    val cubeHistories: List<CubeHistoryDTO>,
    @JsonProperty("next_cursor")
    val nextCursor: String,
)
