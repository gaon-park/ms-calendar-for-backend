package com.maple.heroforbackend.repository

import com.maple.heroforbackend.entity.TFriendship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TFriendshipRepository : JpaRepository<TFriendship, TFriendship.Key> {
    fun findByKeyIn(keys: List<TFriendship.Key>): TFriendship?
}
