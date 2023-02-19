package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TPostLike
import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TPostLikeRepository : JpaRepository<TPostLike, Long> {

    @Query(
        "delete from t_post_like p\n" +
                "where p.post_id = :postId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByPostId(
        @Param("postId") postId: Long
    )

    @Query(
        "select *\n" +
                "from t_post_like l\n" +
                "where l.post_id = :postId",
        nativeQuery = true
    )
    fun findByPostId(
        @Param("postId") postId: Long
    ): List<TPostLike>
}
