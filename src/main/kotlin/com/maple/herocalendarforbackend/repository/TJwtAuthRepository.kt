package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TJwtAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TJwtAuthRepository: JpaRepository<TJwtAuth, String> {
    fun findByIdAndExpirationDateAfterAndExpired(id: String, now: LocalDateTime, expired: Boolean): TJwtAuth?
    fun findByAccessKey(accessKey: String): TJwtAuth?
}
