package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.ProfileInterface

data class SearchUserResponse(
    val users: List<ProfileInterface>,
    val fullHit: Long,
) {
    companion object {
        fun convert(users: List<ProfileInterface>, fullHit: Long) = SearchUserResponse(
            users = users,
            fullHit = fullHit
        )
    }
}
