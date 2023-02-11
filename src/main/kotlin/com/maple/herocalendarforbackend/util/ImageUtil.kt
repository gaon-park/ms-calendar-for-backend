package com.maple.herocalendarforbackend.util

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.exception.BaseException
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

class ImageUtil {

    private val prefix = "data:image/jpg;base64, "

    fun toByteString(multipartFile: MultipartFile): String {
        return prefix + String(Base64.getEncoder().encode(multipartFile.bytes))
    }

    fun toByteArray(encodedByteStr: String): ByteArray {
        val data = encodedByteStr.split(",")
        if (data.size > 1) {
            return Base64.getDecoder().decode(data[1].replace(" ", ""))
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }
}
