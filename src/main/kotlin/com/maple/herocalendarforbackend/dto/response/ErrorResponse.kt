package com.maple.herocalendarforbackend.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.maple.herocalendarforbackend.code.BaseResponseCode
import lombok.Builder

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    @JsonProperty("status")
    val status: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("message")
    val message: String,
) {
    companion object {
        fun convert(errorCode: BaseResponseCode): ErrorResponse =
            ErrorResponse(
                status = errorCode.httpStatus.value(),
                code = errorCode.httpStatus.name,
                message = errorCode.message
            )
    }
}
