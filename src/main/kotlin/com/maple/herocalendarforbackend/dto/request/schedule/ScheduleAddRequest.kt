package com.maple.herocalendarforbackend.dto.request.schedule

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_VALUE_OF_MEMBERS
import com.maple.herocalendarforbackend.dto.request.RepeatInfo
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.Builder
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@Builder
data class ScheduleAddRequest(
    @field:NotEmpty
    val title: String,
    @field:NotNull
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val start: LocalDateTime,
    @field:DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm")
    val end: LocalDateTime?,
    val repeatInfo: RepeatInfo?,
    val allDay: Boolean?,
    val note: String?,
    @field:Size(max = MAX_VALUE_OF_MEMBERS)
    val memberIds: List<String>?,
    val isPublic: Boolean,
    val forOfficial: Boolean?,
) {
    @AssertTrue
    fun isStart(): Boolean {
        return when {
            (end == null && allDay == true) -> true
            (end == null) -> false
            else -> end.isAfter(start)
        }
    }
}
