package com.maple.herocalendarforbackend.dto.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import lombok.Builder

@Builder
@JsonSerialize
data class GoogleOAuthTokenInfo(
    @JsonProperty("alg")
    val alg: String,
    @JsonProperty("kid")
    val kid: String,
    @JsonProperty("typ")
    val typ: String,
    @JsonProperty("iss")
    val iss: String,
    @JsonProperty("azp")
    val azp: String,
    @JsonProperty("aud")
    val aud: String,
    @JsonProperty("sub")
    val sub: String,
    @JsonProperty("email")
    val email: String,
    @JsonProperty("email_verified")
    val emailVerified: Boolean,
    @JsonProperty("at_hash")
    val atHash: String,
    @JsonProperty("iat")
    val iat: Long,
    @JsonProperty("exp")
    val exp: Long
)
