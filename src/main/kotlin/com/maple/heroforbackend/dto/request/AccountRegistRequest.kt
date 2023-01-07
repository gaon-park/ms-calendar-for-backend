package com.maple.heroforbackend.dto.request

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty

data class AccountRegistRequest(
    @field:NotEmpty(message = "이메일은 필수 항목입니다.")
    val email: String,

    @field:NotEmpty(message = "비밀번호는 필수 항목입니다.")
    val password: String,

    @field:NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
    val confirmPassword: String,

    val nickName: String?,

    val isPublic: Boolean?,
) {
    @AssertTrue
    fun isConfirmPassword() = confirmPassword == password
}
