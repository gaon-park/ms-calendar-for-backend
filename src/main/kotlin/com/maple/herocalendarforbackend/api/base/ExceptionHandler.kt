package com.maple.herocalendarforbackend.api.base

import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.exception.BaseException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(exception: BaseException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(exception.errorCode.httpStatus)
            .body(ErrorResponse.convert(exception.errorCode))
//
//    @ExceptionHandler(MethodArgumentNotValidException::class)
//    fun handleMappingException(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> =
//        ResponseEntity.status(exception.statusCode.value())
//            .body(
//                ErrorResponse(
//                    exception.statusCode.value(),
//                    exception.toString(),
//                    exception.fieldErrors.joinToString("\n") { it.field + it.defaultMessage }
//                )
//            )
}
