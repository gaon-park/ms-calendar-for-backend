package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TNotification
import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TNotificationRepository : JpaRepository<TNotification, Long> {

    @Query(
        "select *\n" +
                "from t_notification n\n" +
                "where n.user_id = :userId\n" +
                "order by n.created_at desc",
        nativeQuery = true
    )
    fun findByUserId(
        @Param("userId") userId: String
    ): List<TNotification>

    @Query(
        "delete from t_notification n where n.user_id=:userId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByReadAllEvent(
        @Param("userId") userId: String
    )

    @Query(
        "delete from t_notification n where n.user_id=:userId and n.id=:notificationId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByRead(
        @Param("userId") userId: String,
        @Param("notificationId") notificationId: Long,
    )

    @Query(
        "delete from t_notification n\n" +
                "where n.user_id = :userId\n" +
                "and n.new_follower_id = :followerId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByFollowCancel(
        @Param("userId") userId: String,
        @Param("followerId") followerId: String,
    )

    @Query(
        "delete from t_notification n\n" +
                "where n.user_id = :followerId\n" +
                "and n.new_follow_id = :userId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByFollowerDelete(
        @Param("userId") userId: String,
        @Param("followerId") followerId: String,
    )
}
