package com.maple.heroforbackend.repository

import com.maple.heroforbackend.entity.TEmailToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TEmailTokenRepository : JpaRepository<TEmailToken, String> {
    fun findByIdAndExpirationDateAfterAndExpired(id: String, now: LocalDateTime, expired: Boolean): TEmailToken?
}
