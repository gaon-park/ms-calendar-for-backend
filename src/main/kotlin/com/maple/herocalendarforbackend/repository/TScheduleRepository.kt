package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TScheduleGroup
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
                "inner join (\n" +
                "select distinct schedule_id, user_id\n" +
                "from t_schedule_member\n" +
                "where user_id = :user_id\n" +
                ") as m on m.schedule_id = s.id\n" +
                "where s.repeat_start <= :to and s.repeat_end >= :from",
        nativeQuery = true
    )
    fun findByFromToAndUserId(
        @Param("user_id") userId: String,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): List<TSchedule>

    fun findByGroup(group: TScheduleGroup): List<TSchedule>

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
}
