package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "t_cube_api_key")
data class TCubeApiKey(
    @Id
    val userId: String,
    @Column(length = 10000)
    val apiKey: String,
    val expired: Boolean,
    val createdAt: LocalDateTime,
    val expiredAt: LocalDateTime,
)
