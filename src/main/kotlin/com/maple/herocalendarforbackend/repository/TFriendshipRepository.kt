package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TFriendship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TFriendshipRepository : JpaRepository<TFriendship, TFriendship.Key> {
    fun findByKeyIn(keys: List<TFriendship.Key>): TFriendship?
    fun findByKeyRespondentIdAndAcceptedStatus(respondentId: String, acceptedStatus: AcceptedStatus): List<TFriendship>
}
