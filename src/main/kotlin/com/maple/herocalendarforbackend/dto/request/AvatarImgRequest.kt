package com.maple.herocalendarforbackend.dto.request

import lombok.Builder
import org.springframework.web.multipart.MultipartFile

@Builder
data class AvatarImgRequest(
    val avatarImg: MultipartFile
)
