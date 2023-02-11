package com.maple.herocalendarforbackend.dto.request

import lombok.Builder

@Builder
data class ProfileRequest(
    val nickName: String,
    val accountId: String,
    val isPublic: Boolean,
    val avatarImg: String?,
    val world: String,
    val job: String,
    val jobDetail: String
)
