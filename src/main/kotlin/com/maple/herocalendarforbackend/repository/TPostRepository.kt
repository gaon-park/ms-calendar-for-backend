package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TPostRepository : JpaRepository<TPost, Long> {

    @Query(
        "select *\n" +
                "from t_post p\n" +
                "where p.id = :postId\n" +
                "and p.user_id = :userId",
        nativeQuery = true
    )
    fun findByIdAndUserId(
        @Param("postId") postId: Long,
        @Param("userId") userId: String,
    ): TPost?

    @Query(
        "select *\n" +
                "from t_post p\n" +
                "where p.user_id = :userId",
        nativeQuery = true
    )
    fun findByUserId(
        @Param("userId") userId: String,
    ): List<TPost>
}
