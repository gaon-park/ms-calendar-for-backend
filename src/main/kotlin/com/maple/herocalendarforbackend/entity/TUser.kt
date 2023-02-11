package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "t_user")
data class TUser(
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36, nullable = false)
    val id: String? = null,
    @Column(nullable = false)
    val email: String,
    val nickName: String,
    @Column(unique = true)
    val accountId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isPublic: Boolean,
    @Column(length = 10000)
    val avatarImg: String?,
    val world: String,
    val job: String,
    val jobDetail: String,
    val userRole: String?,
) : UserDetails {
    companion object {
        fun generateOAuthSaveModel(email: String) = TUser(
            email = email,
            accountId = email,
            nickName = email,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isPublic = false,
            avatarImg = null,
            world = "",
            job = "",
            jobDetail = "",
            userRole = null
        )
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? =
        AuthorityUtils.createAuthorityList("ROLE_USER")

    override fun getPassword(): String = ""

    override fun getUsername(): String = id!!

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
