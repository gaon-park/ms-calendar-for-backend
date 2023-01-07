package com.maple.heroforbackend.entity

import com.maple.heroforbackend.dto.request.AccountRegistRequest
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

@Entity
@Table(name = "t_user")
data class TUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val email: String,
    val nickName: String,
    @Column(nullable = false)
    val pass: String,
    val verified: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) : UserDetails {
    companion object {
        fun generateInsertModel(request: AccountRegistRequest, passwordEncoder: PasswordEncoder) = TUser(
            email = request.email,
            nickName = request.nickName ?: request.email,
            pass = passwordEncoder.encode(request.password),
            verified = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        fun sample() = TUser(
            email = "do.judo1224@gmail.com",
            nickName = "",
            pass = "",
            verified = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? =
        AuthorityUtils.createAuthorityList("ROLE_USER")

    override fun getPassword(): String = pass

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
