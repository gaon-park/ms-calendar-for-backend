package com.maple.herocalendarforbackend.dto.request

import lombok.Builder
import org.springframework.web.multipart.MultipartFile

@Builder
data class ProfileRequest(
    val nickName: String,
    val accountId: String,
    val isPublic: Boolean,
    val avatarImg: MultipartFile?
)
