package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TScheduleMemberGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@Suppress("EmptyClassBlock")
interface TScheduleGroupRepository : JpaRepository<TScheduleMemberGroup, Long> {
}
