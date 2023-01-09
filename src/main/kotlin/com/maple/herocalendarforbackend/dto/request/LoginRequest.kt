package com.maple.herocalendarforbackend.dto.request

import jakarta.validation.constraints.NotEmpty

data class LoginRequest(
    @field:NotEmpty(message = "이메일은 필수 항목입니다.")
    val email: String,
    @field:NotEmpty(message = "비밀번호는 필수 항목입니다.")
    val password: String,
)
