package com.maple.herocalendarforbackend.code

import org.springframework.http.HttpStatus

enum class BaseResponseCode(
    val httpStatus: HttpStatus,
    val message: String,
) {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다."),
    WAITING_FOR_RESPONDENT(HttpStatus.BAD_REQUEST, "이미 요청을 보내, 상대방의 수락을 대기중입니다."),
    DATA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB 데이터에 오류가 있습니다. 관리자에게 문의해주세요."),
    OK(HttpStatus.OK, "요청 성공"),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "유효기간이 만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.")
}
