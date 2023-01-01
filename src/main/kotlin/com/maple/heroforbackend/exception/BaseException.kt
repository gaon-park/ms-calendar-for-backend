package com.maple.heroforbackend.exception

import com.maple.heroforbackend.code.BaseResponseCode

open class BaseException(
    val errorCode: BaseResponseCode
) : RuntimeException()
