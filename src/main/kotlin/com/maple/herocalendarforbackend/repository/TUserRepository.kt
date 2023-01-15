package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?
    fun findByIdIn(ids: List<String>): List<TUser>

    @Query(
        "select *\n" +
                "from t_user t\n" +
                "where (t.email= :value or t.nick_name like %:value%)\n" +
                "and t.is_public= :isPublic",
        nativeQuery = true
    )
    fun findByEmailOrNickNameAndIsPublic(
        @Param("value") value: String,
        @Param("isPublic") isPublic: Boolean
    ): List<TUser>

    @Query(
        "select *\n" +
                "from t_user u\n" +
                "where u.verified = false\n" +
                "and u.created_at <= :maximumIssuance",
        nativeQuery = true
    )
    fun findByNotVerified(@Param("maximumIssuance") maximumIssuance: Date): List<TUser>
}
