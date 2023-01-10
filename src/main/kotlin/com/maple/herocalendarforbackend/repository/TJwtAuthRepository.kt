package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TJwtAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TJwtAuthRepository: JpaRepository<TJwtAuth, String> {
    @Query(
        "select * from t_jwt_auth t where t.user_id= :user_id",
        nativeQuery = true
    )
    fun findByUserPk(@Param("user_id") userPk: String): TJwtAuth?
}
