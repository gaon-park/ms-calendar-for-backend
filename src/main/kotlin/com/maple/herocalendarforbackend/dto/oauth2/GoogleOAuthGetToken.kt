package com.maple.herocalendarforbackend.dto.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import lombok.Builder

@Builder
@JsonSerialize
class GoogleOAuthGetToken(
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("scope")
    val scope: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("id_token")
    val idToken: String,
)
