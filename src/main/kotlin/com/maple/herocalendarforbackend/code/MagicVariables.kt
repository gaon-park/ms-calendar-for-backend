package com.maple.herocalendarforbackend.code

object MagicVariables {
    // JWT 액세스 토큰 유효 시간: 2시간
    const val JWT_ACCESS_TOKEN_EXPIRATION_TIME_VALUE = 60 * 60 * 2 * 1000L

    // JWT 리프레시 토큰 유효 시간: 2주
    const val JWT_REFRESH_TOKEN_EXPIRATION_WEEK_VALUE = 2L

    // 초대 최대 인원 수: 50
    const val MAX_VALUE_OF_MEMBERS = 50

    const val SEARCH_DEFAULT_OFFSET = 0
    const val SEARCH_DEFAULT_LIMIT = 10

    const val MAX_LENGTH_OF_USER_COLUMN = 15

    const val MAX_SEARCH_LIMIT: Long = 1000

    const val AUTHORIZATION_REFRESH_JWT = "X-AUTH-REFRESH-TOKEN"

    const val GCS_BASE_URL = "https://storage.googleapis.com/ms-hero-profile/"

    const val RED_LEGENDARY_GRADE_UP = 0.3000
    const val BLACK_LEGENDARY_GRADE_UP = 1.2000
    const val ADDITIONAL_LEGENDARY_GRADE_UP = 0.6000

    const val RED_UNIQUE_GRADE_UP = 1.8000
    const val BLACK_UNIQUE_GRADE_UP = 3.5000
    const val ADDITIONAL_UNIQUE_GRADE_UP = 1.9608

    const val CAN_SEARCH_START_MINUS_MONTH = 1L
}
