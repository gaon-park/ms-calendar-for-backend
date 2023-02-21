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
    fun getAvatarImg(): String?
    fun getHeFollowMe(): String?
    fun getIamFollowHim(): String?
    fun getNotificationFlg(): Boolean
    fun getRole(): String?
}
