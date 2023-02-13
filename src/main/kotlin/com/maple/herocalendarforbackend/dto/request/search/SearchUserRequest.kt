package com.maple.herocalendarforbackend.dto.request.search

import lombok.Builder

@Builder
data class SearchUserRequest(
    val keyword: String?,
    val world: String?,
    val job: String?,
    val jobDetail: String?,
)
