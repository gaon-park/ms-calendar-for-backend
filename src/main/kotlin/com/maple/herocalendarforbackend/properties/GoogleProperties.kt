package com.maple.herocalendarforbackend.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@PropertySource("classpath:googleOAuth\${spring.profiles.active}.properties")
data class GoogleProperties(
    @Value("\${google.clientId}")
    val clientId: String,
    @Value("\${google.clientSecretKey}")
    val clientSecretKey: String,
    @Value("\${google.redirectUrl}")
    val redirectUrl: String,
    @Value("\${google.grantType}")
    val grantType: String,
    @Value("\${google.scope}")
    val scope: String,
)
