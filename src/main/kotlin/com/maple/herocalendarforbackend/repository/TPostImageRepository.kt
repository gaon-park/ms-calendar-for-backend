package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TPostImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TPostImageRepository : JpaRepository<TPostImage, Long> {

    @Query(
        "delete from t_post_image p\n" +
                "where p.post_id = :postId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByPostId(
        @Param("postId") postId: Long
    )

    @Query(
        "select p.path" +
                "from t_post_image p\n" +
                "where p.post_id = :postId",
        nativeQuery = true
    )
    fun findPathsByPostId(
        @Param("postId") postId: Long
    ): List<String>
}
