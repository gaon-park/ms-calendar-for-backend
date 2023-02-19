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
    fun upload(userId: String, postId: Long, data: List<ByteArray>, bucketName: String): List<String> {
        val storage = getStorage()
        val acl = listOf(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))
        return data.map {
            val filePath = "$userId/$postId/${getRandomString()}.png"
            val blobId = BlobId.of(bucketName, filePath)
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setAcl(acl)
                .setContentType("image/png")
                .build()
            storage.create(
                blobInfo,
                it
            )

            "$GCS_BASE_URL$filePath"
        }
    }

    fun delete(path: String, bucketName: String) {
        val storage = getStorage()
        val blobPage = storage.list(bucketName, Storage.BlobListOption.prefix(path))
        val blobIdList = mutableListOf<BlobId>()
        for (blob in blobPage.iterateAll()) {
            blobIdList.add(blob.blobId)
        }
        storage.delete(blobIdList)
    }

    @Async
    fun upload(userId: String, byteArray: ByteArray, bucketName: String): String {
        val storage = getStorage()
        val filePath = "$userId/${getRandomString()}.png"
        val blobId = BlobId.of(bucketName, filePath)
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

    fun removeUnusedImg(filePath: String, bucketName: String) {
        val storage = getStorage()
        val blobId = BlobId.of(bucketName, filePath.replace(GCS_BASE_URL, ""))
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
