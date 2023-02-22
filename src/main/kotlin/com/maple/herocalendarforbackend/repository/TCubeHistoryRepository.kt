package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Suppress("EmptyClassBlock")
@Repository
interface TCubeHistoryRepository : JpaRepository<TCubeHistory, Long> {

}
