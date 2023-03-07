package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeCountHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Suppress("EmptyClassBlock")
@Repository
interface TCubeCountHistoryRepository : JpaRepository<TCubeCountHistory, Long> {


}
