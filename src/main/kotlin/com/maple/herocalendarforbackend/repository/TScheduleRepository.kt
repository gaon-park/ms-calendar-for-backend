package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@Suppress("EmptyClassBlock")
interface TScheduleRepository : JpaRepository<TSchedule, Long> {}
