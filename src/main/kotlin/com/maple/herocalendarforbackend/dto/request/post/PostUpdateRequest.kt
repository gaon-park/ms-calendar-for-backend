package com.maple.herocalendarforbackend.dto.request.post

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_NOTE_LENGTH
import lombok.Builder
import org.hibernate.validator.constraints.Length

@Builder
data class PostUpdateRequest(
    val postId: Long,
    @field:Length(max = MAX_NOTE_LENGTH)
    val note: String,
)
