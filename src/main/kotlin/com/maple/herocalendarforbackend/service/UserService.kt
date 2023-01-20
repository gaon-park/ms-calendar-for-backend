package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TUserRepository
import com.maple.herocalendarforbackend.util.MapleGGUtil
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

    fun findPublicByEmailOrNickName(user: String): List<TUser> =
        tUserRepository.findByEmailOrNickNameAndIsPublic(user)

    fun findById(id: String): TUser =
        tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }

    @Transactional
    fun updateProfile(id: String, request: ProfileRequest): TUser {
        val user = findById(id)
        var avatarImg = user.avatarImg
        if (user.nickName != request.nickName && !request.nickName.contains("@")) {
            val mapleGGUtil = MapleGGUtil()
            mapleGGUtil.getAvatarImg(request.nickName)?.let {
                avatarImg = it
            }
        }
        return tUserRepository.save(
            findById(id).copy(
                nickName = request.nickName,
                isPublic = request.isPublic,
                avatarImg = avatarImg,
                updatedAt = LocalDateTime.now()
            )
        )
    }

    fun reloadAvatarImg(id: String): TUser {
        val user = findById(id)
        // 초기 상태가 아니라면
        if (user.nickName != user.email && !user.nickName.contains("@")) {
            val mapleGGUtil = MapleGGUtil()
            mapleGGUtil.getAvatarImg(user.nickName)?.let {
                if (user.avatarImg != it) {
                    return updateAvatarImg(user, it)
                }
            } ?: throw BaseException(BaseResponseCode.GG_DATA_LOAD_FAILED)
        }
        return user
    }

    @Transactional
    private fun updateAvatarImg(user: TUser, avatarImg: String): TUser =
        tUserRepository.save(
            user.copy(
                avatarImg = avatarImg,
                updatedAt = LocalDateTime.now()
            )
        )
}
