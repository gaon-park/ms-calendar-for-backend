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
                "where requester_id= :userId\n" +
                "or respondent_id= :userId",
        nativeQuery = true
    )
    fun findByUserId(@Param("userId") userId: String): List<TFriendship>
}
