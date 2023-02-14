package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.IProfile

data class IProfileResponse(
    val profile: IProfile,
    val follow: List<IProfile>,
    val follower: List<IProfile>,
    val acceptFollowCount: Long,
    val acceptFollowerCount: Long,
)
