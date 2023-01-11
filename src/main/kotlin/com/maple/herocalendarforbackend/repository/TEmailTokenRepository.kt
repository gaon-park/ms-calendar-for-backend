package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TEmailToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TEmailTokenRepository : JpaRepository<TEmailToken, String> {
    fun findByIdAndExpirationDateAfterAndExpired(id: String, now: LocalDateTime, expired: Boolean): TEmailToken?

    @Query(
        "select *\n" +
                "from t_email_token t\n" +
                "where t.expired=true\n" +
                "or t.expiration_date<= :now",
        nativeQuery = true
    )
    fun findExpired(@Param("now") now: LocalDateTime): List<TEmailToken>
}
