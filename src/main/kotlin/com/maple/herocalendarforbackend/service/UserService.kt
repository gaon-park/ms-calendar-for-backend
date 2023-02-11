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

    fun findByKeywordLike(keyword: String, loginUserId: String?) =
        tUserRepository.findByKeywordLike("%$keyword%", loginUserId ?: "")

    fun findById(id: String): TUser =
        tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }

    @Transactional
    fun updateProfile(id: String, request: ProfileRequest): TUser {
        val user = findById(id)
        val avatarImg = request.avatarImg?.let {
            if (it.isNotEmpty() && it != user.avatarImg) {
                GCSUtil().upload(
                    id,
                    ImageUtil().toByteArray(request.avatarImg)
                )
            } else null
        }
        if (user.accountId != request.accountId && !accountIdDuplicateCheck(request.accountId)) {
            throw BaseException(BaseResponseCode.DUPLICATED_ACCOUNT_ID)
        }
        return if (diffCheck(user, request) || avatarImg != null) {
            tUserRepository.save(
                user.copy(
                    nickName = request.nickName.replace(" ", ""),
                    accountId = request.accountId.replace(" ", ""),
                    isPublic = request.isPublic,
                    avatarImg = avatarImg ?: user.avatarImg,
                    world = request.world,
                    job = request.job,
                    jobDetail = request.jobDetail,
                    updatedAt = LocalDateTime.now()
                )
            )
        } else user
    }

    private fun diffCheck(user: TUser, request: ProfileRequest): Boolean {
        return when {
            request.nickName.isNotEmpty() && user.nickName != request.nickName -> true
            request.accountId.isNotEmpty() && user.accountId != request.accountId -> true
            request.world != user.world -> true
            request.job != user.job -> true
            request.jobDetail != user.jobDetail -> true
            user.isPublic != request.isPublic -> true

            else -> false
        }
    }

    private fun accountIdDuplicateCheck(accountId: String): Boolean =
        tUserRepository.findByAccountId(accountId) == null
}
