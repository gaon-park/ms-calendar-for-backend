package com.maple.herocalendarforbackend.code

object MagicVariables {
    // JWT 액세스 토큰 유효 시간: 1시간
    const val JWT_ACCESS_TOKEN_EXPIRATION_TIME_VALUE = 60 * 60 * 1000L

    // JWT 리프레시 토큰 유효 시간: 2주
    const val JWT_REFRESH_TOKEN_EXPIRATION_WEEK_VALUE = 2L

    // 초대 가능한 최대 인원 수: 30
    const val MAX_VALUE_OF_MEMBERS = 30

    const val AUTHORIZATION_REFRESH_JWT = "XXX-AUTH-REFRESH-TOKEN"
}
