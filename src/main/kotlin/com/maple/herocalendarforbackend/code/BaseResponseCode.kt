package com.maple.herocalendarforbackend.code

import org.springframework.http.HttpStatus

enum class BaseResponseCode(
    val httpStatus: HttpStatus,
    val message: String,
) {
    DUPLICATED_ACCOUNT_ID(HttpStatus.BAD_REQUEST, "accountId 가 중복되어 DB갱신에 실패했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NOT_FOUND(HttpStatus.BAD_REQUEST, "데이터가 존재하지 않습니다."),
    DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB 데이터에 오류가 있습니다. 관리자에게 문의해주세요."),

    ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "이미 친구입니다."),
    WAITING_RESPONSE(HttpStatus.BAD_REQUEST, "이미 요청을 보내 상대방의 응답을 기다리고 있습니다."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "유효기간이 만료된 토큰입니다."),
    NO_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레스 토큰 정보가 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "액세스 토큰 정보가 없습니다."),
}
