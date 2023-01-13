package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.code.AcceptedStatus
import lombok.Builder

@Builder
data class FriendResponse(
    val id: String?,
    val email: String,
    val nickName: String,
    val acceptedStatus: AcceptedStatus
)
