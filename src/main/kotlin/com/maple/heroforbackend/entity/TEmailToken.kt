package com.maple.heroforbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "t_email_token")
data class TEmailToken(
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    val id: String? = null,
    @Column
    val userId: Long,
    @Column
    val expired: Boolean,
    @Column
    val expirationDate: LocalDateTime
) {
    companion object {
        private const val EMAIL_TOKEN_EXPIRATION_TIME_VALUE = 5L

        fun generate(userId: Long) = TEmailToken(
            userId = userId,
            expired = false,
            expirationDate = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_TIME_VALUE)
        )
    }

    fun setTokenToUsed() = this.copy(expired = true)
}
