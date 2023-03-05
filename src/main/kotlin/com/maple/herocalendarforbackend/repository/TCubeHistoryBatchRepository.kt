package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeHistoryBatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TCubeHistoryBatchRepository : JpaRepository<TCubeHistoryBatch, TCubeHistoryBatch.BatchKey> {

    @Query(
        "select * \n" +
                "from t_cube_history_batch b\n" +
                "where b.user_id = :userId\n" +
                "order by b.batch_date desc\n" +
                "limit 1",
        nativeQuery = true
    )
    fun findByUserIdLast(
        @Param("userId") userId: String
    ): TCubeHistoryBatch?

    @Query(
        "select b.user_id as user_id, max(b.batch_date) as batch_date\n" +
                "from t_cube_history_batch b\n" +
                "where b.user_id in :userIds\n" +
                "group by b.user_id",
        nativeQuery = true
    )
    fun findByUserIdInLast(
        @Param("userIds") userIds: List<String>,
    ): List<TCubeHistoryBatch>

    @Query(
        "delete from t_cube_history_batch b where b.user_id = :userId",
        nativeQuery = true
    )
    @Modifying
    @Transactional
    fun deleteByAccount(
        @Param("userId") userId: String,
    )
}
