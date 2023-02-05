package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TUserRepository
import com.maple.herocalendarforbackend.util.GCSUtil
import com.maple.herocalendarforbackend.util.ImageUtil
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val tUserRepository: TUserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails? = username?.let {
        tUserRepository.findByEmail(it)
    }

    fun findByAccountIdLike(keyword: String) =
        tUserRepository.findByAccountIdLike("%$keyword%")

    fun findById(id: String): TUser =
        tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            val entity = it.get()
            entity.copy(
                avatarImg = ImageUtil().readToByteStringFromGCS(entity.avatarImg)
            )
        }

    @Transactional
    fun updateProfile(id: String, request: ProfileRequest): TUser {
        val user = findById(id)
        val avatarImg = request.encodedImg?.let {
            GCSUtil().upload(
                id,
                ImageUtil().toByteArray(request.encodedImg)
            )
        }
        return if (diffCheck(user, request)) {
            tUserRepository.save(
                user.copy(
                    nickName = request.nickName ?: user.nickName,
                    accountId = request.accountId ?: user.accountId,
                    isPublic = request.isPublic ?: user.isPublic,
                    avatarImg = avatarImg ?: user.avatarImg,
                    updatedAt = LocalDateTime.now()
                )
            )
        } else user
    }

    fun diffCheck(user: TUser, request: ProfileRequest): Boolean {
        return when {
            user.nickName != request.nickName -> true
            user.accountId != request.accountId -> true
            user.isPublic != request.isPublic -> true
            else -> false
        }
    }

    fun accountIdDuplicateCheck(accountId: String): Boolean =
        tUserRepository.findByAccountId(accountId) == null
}
