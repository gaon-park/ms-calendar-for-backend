package com.maple.heroforbackend.api.base

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.heroforbackend.dto.response.ErrorResponse
import com.maple.heroforbackend.dto.response.LoginResponse
import com.maple.heroforbackend.exception.BaseException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(exception: BaseException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(exception.errorCode.httpStatus)
            .body(ErrorResponse.convert(exception.errorCode))
}
