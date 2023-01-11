package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
@Suppress("EmptyClassBlock")
interface TScheduleRepository : JpaRepository<TSchedule, Long> {
    @Query(
        "select *\n" +
                "from t_schedule s\n" +
                "inner join t_schedule_member m \n" +
                "on s.id= m.schedule_id\n" +
                "and m.user_id= :user_id\n" +
                "where (s.start <= :to or s.end >= :to) \n" +
                "and (s.start >= :from or s.end >= :from)",
        nativeQuery = true
    )
    fun findByFromToAndUserId(
        @Param("user_id") userId: String,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate
    ): List<TSchedule>
}
