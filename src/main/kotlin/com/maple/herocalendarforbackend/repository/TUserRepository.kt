package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?
    fun findByEmailAndVerified(email: String, verified: Boolean): TUser?
    fun findByEmailIn(emails: List<String>): List<TUser>

    @Query(
        "select * from t_user t where (t.email= :value or t.nick_name like %:value%) and t.is_public= :isPublic",
        nativeQuery = true
    )
    fun findByEmailOrNickNameAndIsPublic(
        @Param("value") value: String,
        @Param("isPublic") isPublic: Boolean
    ): List<TUser>
}
