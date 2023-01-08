package com.maple.heroforbackend.repository

import com.maple.heroforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?
    fun findByEmailAndVerified(email: String, verified: Boolean): TUser?
    fun findByEmailIn(emails: List<String>): List<TUser>
}
