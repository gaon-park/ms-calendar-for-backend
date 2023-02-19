package com.maple.herocalendarforbackend.dto.request.post

import lombok.Builder

@Builder
data class PostRequest(
    val postId: Long,
)
