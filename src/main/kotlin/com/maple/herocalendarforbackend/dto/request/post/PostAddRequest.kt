package com.maple.herocalendarforbackend.dto.request.post

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_IMAGE_UPLOAD
import com.maple.herocalendarforbackend.code.MagicVariables.MAX_NOTE_LENGTH
import jakarta.validation.constraints.Size
import lombok.Builder
import org.hibernate.validator.constraints.Length

@Builder
data class PostAddRequest(
    @field:Length(max = MAX_NOTE_LENGTH)
    val note: String,
    @field:Size(max = MAX_IMAGE_UPLOAD)
    val postImages: List<String>,
)
