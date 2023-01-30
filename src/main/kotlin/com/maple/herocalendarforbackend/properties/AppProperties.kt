package com.maple.herocalendarforbackend.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@PropertySource("classpath:application\${spring.profiles.active}.properties")
data class AppProperties(
    @Value("\${jwt.secretKey}")
    val jwtSecretKey: String,
)
