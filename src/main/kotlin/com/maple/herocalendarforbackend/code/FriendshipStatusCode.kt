package com.maple.herocalendarforbackend.code

enum class FriendshipStatusCode(
    val status: Int,
    val info: String,
) {
    FRIEND(0, "친구"),
    WAITING_MY_RESPONSE(1, "내 응답 대기중"),
    WAITING_OPP_RESPONSE(2, "상대방 응답 대기중")
}
