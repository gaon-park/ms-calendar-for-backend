package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface TScheduleRepository : JpaRepository<TSchedule, Long> {
    @Query(
        "select *\n" +
                "from t_schedule s\n" +
                "where (:userId in (\n" +
                "   select m.user_id\n" +
                "   from t_schedule_member m\n" +
                "   where m.group_id = s.member_group_id\n" +
                ") or s.owner_id = :userId)\n" +
                "and s.start <= :to and s.end >= :from",
        nativeQuery = true
    )
    fun findByFromToAndUserId(
        @Param("userId") userId: String,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): List<TSchedule>

    fun findByParentId(parentId: Long?): List<TSchedule>

    @Query(
        "select *\n" +
                "from t_schedule s\n" +
                "where s.group_id = :groupId\n" +
                "and s.start >= :after",
        nativeQuery = true
    )
    fun findByGroupAndAfterDay(
        @Param("groupId") groupId: Long?,
        @Param("after") after: LocalDateTime
    ): List<TSchedule>

    fun findByOwnerId(ownerId: String): List<TSchedule>
}
