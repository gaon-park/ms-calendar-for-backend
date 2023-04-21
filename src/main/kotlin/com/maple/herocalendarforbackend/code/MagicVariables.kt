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

    // 레전드리 등급업
    const val MYUNGJANG_LEGENDARY_GRADE_UP = 0.1996
    const val RED_LEGENDARY_GRADE_UP = 0.3000
    const val BLACK_LEGENDARY_GRADE_UP = 1.4000
    const val ADDITIONAL_LEGENDARY_GRADE_UP = 0.6000

    // 유니크 등급업
    const val JANGYIN_UNIQUE_GRADE_UP = 1.1858
    const val MYUNGJANG_UNIQUE_GRADE_UP = 1.6959
    const val RED_UNIQUE_GRADE_UP = 1.8000
    const val BLACK_UNIQUE_GRADE_UP = 3.5000
    const val ADDITIONAL_UNIQUE_GRADE_UP = 1.9608

    // 에픽 등급업
    const val SUSANG_EPIC_GRADE_UP = 0.9901
    const val JANGYIN_EPIC_GRADE_UP = 4.7619
    const val MYUNGJANG_EPIC_GRADE_UP = 7.9994
    const val RED_EPIC_GRADE_UP = 6.000
    const val BLACK_EPIC_GRADE_UP = 15.000
    const val ADDITIONAL_EPIC_GRADE_UP = 4.7619

    const val CAN_SEARCH_START_MINUS_MONTH = 1L
}
