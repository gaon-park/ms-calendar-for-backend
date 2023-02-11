package com.maple.herocalendarforbackend.util

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Acl
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.maple.herocalendarforbackend.code.MagicVariables.GCS_BASE_URL
import org.springframework.scheduling.annotation.Async
import java.io.FileInputStream

class GCSUtil {

    @Async
    fun upload(userId: String, byteArray: ByteArray): String {
        val storage = getStorage()
        val filePath = "$userId/${getRandomString()}.png"
        val blobId = BlobId.of("ms-hero-profile", filePath)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setAcl(listOf(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
            .setContentType("image/png")
            .build()
        storage.create(
            blobInfo,
            byteArray
        )

        return "$GCS_BASE_URL$filePath"
    }

    fun removeUnusedImg(filePath: String) {
        val storage = getStorage()
        val blobId = BlobId.of("ms-hero-profile", filePath.replace(GCS_BASE_URL, ""))
        storage.delete(blobId)
    }

    @Suppress("MagicNumber")
    private fun getRandomString(): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..10)
            .map { charset.random() }
            .joinToString("")
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
