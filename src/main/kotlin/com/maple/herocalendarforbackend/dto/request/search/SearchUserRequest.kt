package com.maple.herocalendarforbackend.dto.request.search

import com.maple.herocalendarforbackend.dto.request.PageInfo
import lombok.Builder

@Builder
data class SearchUserRequest(
    val keyword: String,
    val pageInfo: PageInfo?,
)
