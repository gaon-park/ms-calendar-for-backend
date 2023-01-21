package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.entity.TScheduleMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TScheduleMemberRepository : JpaRepository<TScheduleMember, TScheduleMember.GroupKey> {
    fun findByGroupKeyGroupId(groupId: Long): List<TScheduleMember>

    @Query(
        "select *\n" +
                "from t_schedule_member m\n" +
                "where (\n" +
                "   select s.member_group_id\n" +
                "   from t_schedule s\n" +
                "   where s.id = :scheduleId\n" +
                ") and m.user_id = :userId\n" +
                "and accepted_status != :notEqualStatus",
        nativeQuery = true
    )
    fun findByScheduleIdAndUserIdAndAcceptedStatus(
        @Param("scheduleId") scheduleId: Long,
        @Param("userId") userId: String,
        @Param("notEqualStatus") notEqualStatus: String
    ): TScheduleMember?

    fun deleteByGroupKeyGroupIdIn(groupIds: List<Long>)

//    fun deleteByScheduleKeyScheduleId(scheduleId: Long)
//    fun deleteByScheduleKey(key: TScheduleMember.GroupKey)
//    fun findByScheduleKeyUserIdAndAcceptedStatus(userId: String, acceptedStatus: AcceptedStatus): List<TScheduleMember>
//    fun findByScheduleKeyScheduleId(scheduleId: Long): List<TScheduleMember>
//    fun findByScheduleKeyScheduleIdIn(scheduleIds: List<Long>): List<TScheduleMember>
//    fun findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId: Long, userId: String): TScheduleMember?
}
