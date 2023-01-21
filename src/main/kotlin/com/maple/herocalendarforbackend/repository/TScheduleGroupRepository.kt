package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TScheduleGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TScheduleGroupRepository : JpaRepository<TScheduleGroup, Long> {
}