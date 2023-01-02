package com.maple.heroforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.maple.heroforbackend.entity.TJwtAuth
import lombok.Builder

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponse(
    @JsonProperty("access_token")
    val accessToken: String?
)
