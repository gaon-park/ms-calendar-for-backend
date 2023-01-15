package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TScheduleOwnerRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TScheduleOwnerRequestRepository: JpaRepository<TScheduleOwnerRequest, TScheduleOwnerRequest.OwnerRequestId> {

    @Query(
        "select *\n" +
                "from t_schedule_owner_request t\n" +
                "where t.schedule_id= :scheduleId\n" +
                "and t.respondent_id= :respondentId",
        nativeQuery = true
    )
    fun findRequest(
        @Param("scheduleId") scheduleId: Long,
        @Param("respondentId") respondentId: String
    ): TScheduleOwnerRequest?

    fun findByRespondentId(respondentId: String): List<TScheduleOwnerRequest>
}
