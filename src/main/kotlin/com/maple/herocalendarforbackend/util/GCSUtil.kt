package com.maple.herocalendarforbackend.util

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Acl
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import org.springframework.scheduling.annotation.Async
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream

class GCSUtil {

    @Async
    fun upload(userId: String, img: MultipartFile): String {
        val storage = StorageOptions.newBuilder()
            .setProjectId("ms-calendar-374715")
            .setCredentials(
                GoogleCredentials.fromStream(
                    FileInputStream("src/main/resources/gcs.json")
                )
            )
            .build()
            .service
        val filePath = "$userId.png"
        val blobId = BlobId.of("ms-hero-profile", filePath)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setAcl(listOf(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
            .setContentType("image/png")
            .build()
        storage.create(
            blobInfo,
            img.bytes
        )

        return "https://storage.googleapis.com/ms-hero-profile/$filePath"
    }
}
