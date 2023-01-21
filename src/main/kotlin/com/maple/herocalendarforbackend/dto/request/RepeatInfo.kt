package com.maple.herocalendarforbackend.dto.request

import com.maple.herocalendarforbackend.code.RepeatCode
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import lombok.Builder
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

@Builder
data class RepeatInfo(
    val repeatCode: RepeatCode,
    @field:NotNull
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    val end: LocalDate?
) {
    @AssertTrue
    fun isEnd(): Boolean {
        if (end != null) {
            return end.isAfter(LocalDate.now())
        }
        return true
    }
}
