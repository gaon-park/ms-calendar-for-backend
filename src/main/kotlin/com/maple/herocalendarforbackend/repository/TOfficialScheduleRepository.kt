package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TOfficialSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TOfficialScheduleRepository: JpaRepository<TOfficialSchedule, Long> {

    @Query(
        "select *\n" +
                "from t_official_schedule s\n" +
                "where s.start <= :to and s.end >= :from",
        nativeQuery = true
    )
    fun findByFromTo(
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
    ): List<TOfficialSchedule>
}
