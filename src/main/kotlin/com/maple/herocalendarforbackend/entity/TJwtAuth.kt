package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.MagicVariables.JWT_REFRESH_TOKEN_EXPIRATION_WEEK_VALUE
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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
    val refreshToken: String? = null,
    val expired: Boolean,
    val expirationDate: LocalDateTime,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val userPk: TUser,
) {
    companion object {
        fun generate(now: LocalDateTime, userPk: TUser) = TJwtAuth(
            expired = false,
            expirationDate = now.plusWeeks(JWT_REFRESH_TOKEN_EXPIRATION_WEEK_VALUE),
            userPk = userPk
        )
    }
}
