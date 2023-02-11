package com.maple.herocalendarforbackend.util

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.exception.BaseException
import java.util.Base64

class ImageUtil {
    fun toByteArray(encodedByteStr: String): ByteArray {
        val data = encodedByteStr.split(",")
        if (data.size > 1) {
            return Base64.getDecoder().decode(data[1].replace(" ", ""))
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }
}
