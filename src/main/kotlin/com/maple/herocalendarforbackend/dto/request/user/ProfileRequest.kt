package com.maple.herocalendarforbackend.dto.request.user

import com.maple.herocalendarforbackend.code.MagicVariables
import lombok.Builder
import org.hibernate.validator.constraints.Length

@Builder
data class ProfileRequest(
    @field:Length(max = MagicVariables.MAX_LENGTH_OF_USER_COLUMN)
    val nickName: String,
    @field:Length(max = MagicVariables.MAX_LENGTH_OF_USER_COLUMN)
    val accountId: String,
    val isPublic: Boolean,
    val avatarImg: String?,
    val world: String,
    val job: String,
    val jobDetail: String,
    val notificationFlg: Boolean,
)
