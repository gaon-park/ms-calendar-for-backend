package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.MagicVariables.EMAIL_TOKEN_EXPIRATION_HOUR_VALUE
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
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
    val userId: String,
    val expired: Boolean,
    val expirationDate: LocalDateTime
) {
    companion object {

        fun generate(userId: String) = TEmailToken(
            userId = userId,
            expired = false,
            expirationDate = LocalDateTime.now().plusHours(EMAIL_TOKEN_EXPIRATION_HOUR_VALUE)
        )
    }
}
