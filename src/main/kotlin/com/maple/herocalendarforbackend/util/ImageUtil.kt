package com.maple.herocalendarforbackend.util

import org.springframework.web.multipart.MultipartFile
import java.util.Base64

class ImageUtil {

    private val prefix = "data:image/jpg;base64, "

    fun toByteString(multipartFile: MultipartFile): String {
        return prefix + String(Base64.getEncoder().encode(multipartFile.bytes))
    }

    fun readToByteStringFromGCS(filePath: String): String {
        return prefix + String(Base64.getEncoder().encode(GCSUtil().readToByteArray(filePath)))
    }

    fun toByteArray(encodedByteStr: String): ByteArray {
        return Base64.getDecoder().decode(encodedByteStr.replace(prefix, ""))
    }
}
