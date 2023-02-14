package com.maple.herocalendarforbackend.entity

import java.time.LocalDateTime

@Suppress("TooManyFunctions")
interface IProfile {
    fun getId(): String
    fun getNickName(): String
    fun getAccountId(): String
    fun getCreatedAt(): LocalDateTime
    fun getUpdatedAt(): LocalDateTime
    fun getIsPublic(): Boolean
    fun getWorld(): String
    fun getJob(): String
    fun getJobDetail(): String
    fun avatarImg(): String?
    fun getHeFollowMe(): String?
    fun getIFollowHim(): String?
}
