package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TUserRepository
import com.maple.herocalendarforbackend.util.GCSUtil
import org.hibernate.exception.ConstraintViolationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
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
            it.get()
        }

    @Transactional
    fun updateProfile(id: String, request: ProfileRequest): TUser {
        val user = findById(id)
        val avatarImg = if (request.avatarImg is MultipartFile) {
            request.avatarImg.let {
                GCSUtil().upload(id, request.avatarImg)
            }
        } else user.avatarImg
        return if (diffCheck(user, request)) {
            tUserRepository.save(
                user.copy(
                    nickName = request.nickName,
                    accountId = request.accountId,
                    isPublic = request.isPublic,
                    avatarImg = avatarImg,
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
