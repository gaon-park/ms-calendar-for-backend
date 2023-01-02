package com.maple.heroforbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "t_jwt_auth")
data class TJwtAuth(
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    val id: String? = null,
    @Column
    val expired: Boolean,
    @Column
    val expirationDate: LocalDateTime,
    @Column
    val accessKey: String,
) {
    companion object {
        private const val REFRESH_TOKEN_EXPIRATION_WEEKS_VALUE = 2L

        fun generate(accessKey: String, now: LocalDateTime) = TJwtAuth(
            expired = false,
            expirationDate = now.plusWeeks(REFRESH_TOKEN_EXPIRATION_WEEKS_VALUE),
            accessKey = accessKey
        )
    }
}
