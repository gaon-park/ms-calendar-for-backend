package com.maple.herocalendarforbackend.code

enum class FollowAcceptedStatus(
    val status: Int,
    val info: String,
) {
    WAITING(0, "응답 대기중"),
    ACCEPTED(1, "수락"),
}
