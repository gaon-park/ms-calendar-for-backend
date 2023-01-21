package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TScheduleMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TScheduleMemberRepository : JpaRepository<TScheduleMember, TScheduleMember.GroupKey> {
//    fun deleteByScheduleKeyScheduleId(scheduleId: Long)
//    fun deleteByScheduleKey(key: TScheduleMember.GroupKey)
//    fun findByScheduleKeyUserIdAndAcceptedStatus(userId: String, acceptedStatus: AcceptedStatus): List<TScheduleMember>
//    fun findByScheduleKeyScheduleId(scheduleId: Long): List<TScheduleMember>
//    fun findByScheduleKeyScheduleIdIn(scheduleIds: List<Long>): List<TScheduleMember>
//    fun findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId: Long, userId: String): TScheduleMember?
}
