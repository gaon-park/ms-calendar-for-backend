package com.maple.herocalendarforbackend.dto.request

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_SEARCH_LIMIT
import com.maple.herocalendarforbackend.code.MagicVariables.SEARCH_DEFAULT_LIMIT
import com.maple.herocalendarforbackend.code.MagicVariables.SEARCH_DEFAULT_OFFSET

data class PageInfo(
    val limit: Int,
    val offset: Int,
) {
    companion object {
        fun convert(limit: Int?, offset: Int?) = PageInfo(
            limit = when {
                (limit == null) -> SEARCH_DEFAULT_LIMIT
                (limit > MAX_SEARCH_LIMIT) -> MAX_SEARCH_LIMIT
                else -> limit
            } as Int,
            offset = offset ?: SEARCH_DEFAULT_OFFSET
        )
    }
}
