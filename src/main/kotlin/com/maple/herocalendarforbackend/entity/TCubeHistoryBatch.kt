package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "t_cube_history_batch")
data class TCubeHistoryBatch(
    @EmbeddedId
    val batchKey: BatchKey
) {
    companion object {
        fun convert(userId: String, batchDate: LocalDate) = TCubeHistoryBatch(
            batchKey = BatchKey(userId, batchDate)
        )
    }

    @Embeddable
    data class BatchKey(
        val userId: String,
        val batchDate: LocalDate
    )
}
