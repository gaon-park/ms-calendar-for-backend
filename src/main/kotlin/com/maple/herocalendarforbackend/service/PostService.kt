package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.post.PostAddRequest
import com.maple.herocalendarforbackend.dto.request.post.PostRequest
import com.maple.herocalendarforbackend.dto.request.post.PostUpdateRequest
import com.maple.herocalendarforbackend.entity.TPost
import com.maple.herocalendarforbackend.entity.TPostImage
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TPostImageRepository
import com.maple.herocalendarforbackend.repository.TPostLikeRepository
import com.maple.herocalendarforbackend.repository.TPostRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import com.maple.herocalendarforbackend.util.GCSUtil
import com.maple.herocalendarforbackend.util.ImageUtil
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class PostService(
    private val tUserRepository: TUserRepository,
    private val tPostRepository: TPostRepository,
    private val tPostImageRepository: TPostImageRepository,
    private val tPostLikeRepository: TPostLikeRepository,
) {
    private val postBucketName = "ms-hero-post"

    private fun findUserById(id: String): TUser {
        tUserRepository.findById(id).let {
            if (it.isPresent) {
                return it.get()
            } else throw BaseException(BaseResponseCode.USER_NOT_FOUND)
        }
    }

    private fun findById(loginUserId: String, postId: Long): TPost {
        return tPostRepository.findByIdAndUserId(postId, loginUserId)
            ?: throw BaseException(BaseResponseCode.NOT_FOUND)
    }

    fun findByUserIdToIPost(userId: String): List<TPost> {
        return tPostRepository.findByUserId(userId)
    }

    @Transactional
    fun save(loginUserId: String, request: PostAddRequest) {
        val user = findUserById(loginUserId)

        // save db
        val post = tPostRepository.save(TPost.convert(user, request))

        // upload image
        post.id?.let {
            val imageUtil = ImageUtil()
            val filePaths = GCSUtil().upload(
                userId = loginUserId,
                postId = it,
                data = request.postImages.map { img ->
                    imageUtil.toByteArray(img)
                },
                bucketName = postBucketName
            )

            // save image db
            tPostImageRepository.saveAll(TPostImage.convert(it, filePaths))
        }
    }

    @Transactional
    fun update(loginUserId: String, request: PostUpdateRequest) {
        val post = findById(loginUserId, request.postId)
        tPostRepository.save(
            post.copy(note = request.note)
        )
    }

    @Transactional
    fun delete(loginUserId: String, request: PostRequest) {
        val post = findById(loginUserId, request.postId)
        post.id?.let {
            tPostLikeRepository.deleteByPostId(it)
            tPostImageRepository.deleteByPostId(it)
            tPostRepository.deleteById(it)

            GCSUtil().delete("$loginUserId/$it/", postBucketName)
        }
    }
}
