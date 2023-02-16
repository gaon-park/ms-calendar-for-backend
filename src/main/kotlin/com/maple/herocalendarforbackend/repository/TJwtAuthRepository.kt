package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TJwtAuth
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TJwtAuthRepository : JpaRepository<TJwtAuth, String> {
    @Query(
        "select * from t_jwt_auth t where t.user_id= :user_id",
        nativeQuery = true
    )
    fun findByUserPk(@Param("user_id") userPk: String): List<TJwtAuth>

    @Query(
        "delete from t_jwt_auth t where t.user_id = :user_id",
        nativeQuery = true
    )
    @Modifying
    @Transactional
    fun deleteByUserPk(@Param("user_id") userPk: String)

    @Query(
        "delete \n" +
                "from t_jwt_auth t\n" +
                "where t.expired=true\n" +
                "or t.expiration_date<= :now",
        nativeQuery = true
    )
    @Modifying
    fun deleteExpired(@Param("now") now: LocalDateTime)

    @Query(
        "delete from t_jwt_auth t where t.user_id= :user_id",
        nativeQuery = true
    )
    @Modifying
    fun deleteByDeletedAccount(@Param("user_id") userId: String)
}
