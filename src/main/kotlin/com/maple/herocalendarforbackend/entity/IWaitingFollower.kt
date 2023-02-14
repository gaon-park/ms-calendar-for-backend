package com.maple.herocalendarforbackend.entity

import java.time.LocalDateTime

interface IWaitingFollower {
    fun getId(): String
    fun getNickName(): String
    fun getAccountId(): String
    fun getAvatarImg(): String?
    fun getCreatedAt(): LocalDateTime
}
