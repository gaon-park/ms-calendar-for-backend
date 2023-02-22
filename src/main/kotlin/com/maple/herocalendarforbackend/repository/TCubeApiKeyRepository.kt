package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeApiKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Suppress("EmptyClassBlock")
@Repository
interface TCubeApiKeyRepository : JpaRepository<TCubeApiKey, String> {

    @Query(
        "select *\n" +
                "from t_cube_api_key c\n" +
                "where c.user_id = :userId",
        nativeQuery = true
    )
    fun findByUserId(
        @Param("userId") userId: String
    ): TCubeApiKey?
}
