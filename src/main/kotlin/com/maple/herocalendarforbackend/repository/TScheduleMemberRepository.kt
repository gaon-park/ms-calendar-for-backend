package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TScheduleMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TScheduleMemberRepository : JpaRepository<TScheduleMember, Long> {
    fun deleteByScheduleId(scheduleId: Long)
    fun deleteByScheduleIdAndUserId(scheduleId: Long, userId: String)
}
