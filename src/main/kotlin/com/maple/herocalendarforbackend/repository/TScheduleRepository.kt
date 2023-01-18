package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

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
}
