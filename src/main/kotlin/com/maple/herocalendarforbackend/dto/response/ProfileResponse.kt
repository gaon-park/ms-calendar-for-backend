package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.maple.herocalendarforbackend.code.FriendshipStatusCode
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder
import java.time.LocalDateTime

@Builder
data class ProfileResponse(
    val id: String?,
    val nickName: String,
    val accountId: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val createdAt: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    val updatedAt: LocalDateTime,
    val isPublic: Boolean,
    val world: String,
    val job: String,
    val jobDetail: String,
    val avatarImg: String?,
    val holderFlg: Boolean,
    val status: FriendshipStatusCode?,
) {
    companion object {
        fun convert(data: TUser, holderFlg: Boolean, status: FriendshipStatusCode?) = ProfileResponse(
            id = data.id,
            accountId = data.accountId,
            nickName = data.nickName,
            createdAt = data.createdAt,
            updatedAt = data.updatedAt,
            isPublic = data.isPublic,
            world = data.world,
            job = data.job,
            jobDetail = data.jobDetail,
            avatarImg = data.avatarImg,
            holderFlg = holderFlg,
            status = status
        )
    }
}
