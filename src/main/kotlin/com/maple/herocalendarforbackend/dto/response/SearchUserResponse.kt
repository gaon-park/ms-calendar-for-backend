package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.IProfile

data class SearchUserResponse(
    val users: List<IProfile>,
    val fullHit: Long,
) {
    companion object {
        fun convert(users: List<IProfile>, fullHit: Long) = SearchUserResponse(
            users = users,
            fullHit = fullHit
        )
    }
}
