package com.maple.heroforbackend.dto.request

import jakarta.validation.constraints.NotEmpty

data class AccountRegistRequest(
    @NotEmpty(message = "이메일은 필수 항목입니다.")
    val email: String,

    @NotEmpty(message = "비밀번호는 필수 항목입니다.")
    val password: String,

    @NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
    val confirmPassword: String,

    val nickName: String?,
)
