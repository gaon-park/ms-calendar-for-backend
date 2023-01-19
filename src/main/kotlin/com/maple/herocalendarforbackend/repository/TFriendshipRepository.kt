package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TFriendship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TFriendshipRepository : JpaRepository<TFriendship, TFriendship.Key> {
    fun findByKeyIn(keys: List<TFriendship.Key>): TFriendship?
    fun findByKeyRespondentIdAndAcceptedStatus(respondentId: String, acceptedStatus: AcceptedStatus): List<TFriendship>

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where requester_id= :userId \n" +
                "or respondent_id= :userId",
        nativeQuery = true
    )
    fun findByUserId(@Param("userId") userId: String): List<TFriendship>


    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where requester_id= :userId \n" +
                "or respondent_id= :userId ",
        nativeQuery = true
    )
    fun findAllAcceptedStatusByUserId(
        @Param("userId") userId: String,
    ): List<TFriendship>

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where (f.requester_id= :user0 and f.respondent_id= :user1) \n" +
                "or (f.requester_id= :user1 and f.respondent_id= :user0) \n" +
                "and accept_status=\"ACCEPTED\"",
        nativeQuery = true
    )
    fun findFriendship(
        @Param("user0") user0: String,
        @Param("user1") user1: String
    ): TFriendship?
}
