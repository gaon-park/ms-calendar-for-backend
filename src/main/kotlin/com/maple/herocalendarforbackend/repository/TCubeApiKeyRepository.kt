package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeApiKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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

    @Query(
        "select *\n" +
                "from t_cube_api_key c\n" +
                "limit :offset, :limit",
        nativeQuery = true
    )
    fun findByLimitOffset(
        @Param("limit") limit: Int,
        @Param("offset") offset: Long
    ): List<TCubeApiKey>

    @Query(
        "delete from t_cube_api_key c where c.user_id = :userId",
        nativeQuery = true
    )
    @Modifying
    @Transactional
    fun deleteByAccount(
        @Param("userId") userId: String
    )
}
