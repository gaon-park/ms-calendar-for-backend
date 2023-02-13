package com.maple.herocalendarforbackend.dto.response

import java.time.LocalDateTime

data class FollowUser(
    val id: String,
    val accountId: String,
    val nickName: String,
    val avatarImg: String?,
    val world: String,
    val job: String,
    val jobDetail: String,
    val isPublic: Boolean,
    val createdAt: LocalDateTime,
    val heFollowMe: Boolean,
    val iFollowHim: Boolean,
)
