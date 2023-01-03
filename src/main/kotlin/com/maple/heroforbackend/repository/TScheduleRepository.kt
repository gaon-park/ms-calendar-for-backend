package com.maple.heroforbackend.repository

import com.maple.heroforbackend.entity.TSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@Suppress("EmptyClassBlock")
interface TScheduleRepository : JpaRepository<TSchedule, Long> {}
