package com.maple.herocalendarforbackend.util

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.scheduling.annotation.Async
import java.io.FileInputStream

class GCSUtil {

    @Async
    fun upload(userId: String, byteArray: ByteArray): String {
        val storage = getStorage()
        val filePath = "$userId.png"
        val blobId = BlobId.of("ms-hero-profile", filePath)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("image/png")
            .build()
        storage.create(
            blobInfo,
            byteArray
        )
        return filePath
    }

    fun readToByteArray(filePath: String): ByteArray {
        val storage = getStorage()
        val blob = storage.get(BlobId.of("ms-hero-profile", filePath))
        return blob.getContent()
    }

    private fun getStorage(): Storage {
        return StorageOptions.newBuilder()
            .setProjectId("ms-calendar-374715")
            .setCredentials(
                GoogleCredentials.fromStream(
                    FileInputStream("src/main/resources/gcs.json")
                )
            )
            .build()
            .service
    }
}
