package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TFollowRelationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TFollowRelationshipRepository : JpaRepository<TFollowRelationship, TFollowRelationship.Key> {
    @Query(
        "update t_follow_relationship f\n" +
                "set f.accepted_status = :statusValue\n" +
                "where f.requester_id = :requester\n" +
                "and f.respondent_id = :respondent\n" +
                "and f.accepted_status != :statusValue",
        nativeQuery = true
    )
    @Modifying
    fun updateStatus(
        @Param("statusValue") statusValue: String,
        @Param("requester") requesterId: String,
        @Param("respondent") respondentId: String,
    )

    @Query(
        "select *\n" +
                "from t_follow_relationship f\n" +
                "where f.requester_id = :userId",
        nativeQuery = true
    )
    fun findFollowingsByUserId(@Param("userId") userId: String): List<TFollowRelationship>

    @Query(
        "select *\n" +
                "from t_follow_relationship f\n" +
                "where f.respondent_id = :userId",
        nativeQuery = true
    )
    fun findFollowersByUserId(@Param("userId") userId: String): List<TFollowRelationship>

    @Query(
        "select *\n" +
                "from t_follow_relationship f\n" +
                "where f.respondent_id = :userId\n" +
                "and f.accepted_status = \"WAITING\"",
        nativeQuery = true
    )
    fun findWaitingFollowers(@Param("userId") userId: String): List<TFollowRelationship>

    @Query(
        "select *\n" +
                "from t_follow_relationship f\n" +
                "where (f.requester_id= :requester and f.respondent_id= :respondent) \n" +
                "and accept_status=\"ACCEPTED\"",
        nativeQuery = true
    )
    fun followingCheck(
        @Param("requester") requester: String,
        @Param("respondent") respondent: String
    ): TFollowRelationship?
}
