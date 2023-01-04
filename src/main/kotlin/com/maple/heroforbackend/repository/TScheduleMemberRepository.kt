package com.maple.heroforbackend.repository

import com.maple.heroforbackend.entity.TScheduleMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TScheduleMemberRepository : JpaRepository<TScheduleMember, Long> {
    fun deleteByScheduleId(scheduleId: Long)
    fun deleteByScheduleIdAndUserId(scheduleId: Long, userId: Long)
    fun findByUserId(userId: Long): List<TScheduleMember>
}
