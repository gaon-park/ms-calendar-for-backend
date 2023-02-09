package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TScheduleNote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TScheduleNoteRepository : JpaRepository<TScheduleNote, Long> {

    @Query(
        "select *\n" +
                "from t_schedule_note g\n" +
                "where g.id not in (\n" +
                "   select distinct s.note_id\n" +
                "   from t_schedule s\n" +
                ")",
        nativeQuery = true
    )
    fun findUnusedIds(): List<TScheduleNote>
}
