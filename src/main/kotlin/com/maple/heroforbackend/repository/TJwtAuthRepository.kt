package com.maple.heroforbackend.repository

import com.maple.heroforbackend.entity.TJwtAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TJwtAuthRepository: JpaRepository<TJwtAuth, String> {
    fun findByIdAndExpirationDateAfterAndExpired(id: String, now: LocalDateTime, expired: Boolean): TJwtAuth?
    fun findByAccessKey(accessKey: String): TJwtAuth?
}
