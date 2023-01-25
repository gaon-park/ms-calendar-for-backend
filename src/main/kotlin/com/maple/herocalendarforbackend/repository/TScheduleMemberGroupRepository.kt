package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TScheduleMemberGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TScheduleMemberGroupRepository : JpaRepository<TScheduleMemberGroup, Long> {

    @Query(
        "select *\n" +
                "from t_schedule_member_group g\n" +
                "where g.id not in (\n" +
                "   select distinct s.member_group_id\n" +
                "   from t_schedule s\n" +
                ")",
        nativeQuery = true
    )
    fun findUnusedGroupIds(): List<TScheduleMemberGroup>
}
