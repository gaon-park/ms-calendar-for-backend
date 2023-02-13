package com.maple.herocalendarforbackend.code

enum class AcceptedStatus(
    val status: Int,
    val info: String,
) {
    WAITING(0, "응답 대기중"),
    REFUSED(-1, "거절"),
    ACCEPTED(1, "수락"),
}
