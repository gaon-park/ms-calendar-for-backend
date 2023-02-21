package com.maple.herocalendarforbackend.dto.response

data class NotificationResponse(
    val notificationId: Long?,
    val meta: String,
    val title: String,
    val subTitle: String,
    val avatarImg: String?,
    val avatarText: String,
    val linkPath: String,
)
