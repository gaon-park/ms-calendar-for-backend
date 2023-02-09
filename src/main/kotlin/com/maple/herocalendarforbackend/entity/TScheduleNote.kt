package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "t_schedule_note")
data class TScheduleNote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val note: String,
) {
    companion object {
        fun convert(note: String) = TScheduleNote(
            note = note
        )
    }
}
