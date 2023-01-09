package com.maple.herocalendarforbackend.exception

import com.maple.herocalendarforbackend.code.BaseResponseCode

open class BaseException(
    val errorCode: BaseResponseCode
) : RuntimeException()
