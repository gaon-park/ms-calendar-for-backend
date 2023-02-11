package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFollowRelationshipRepository
import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
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
    private val tUserRepository: TUserRepository,
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tFollowRelationshipRepository: TFollowRelationshipRepository,
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
                val gcsUtil = GCSUtil()
                val newImg = gcsUtil.upload(
                    id,
                    ImageUtil().toByteArray(request.avatarImg)
                )

                // 이미지 저장 후, 사용하지 않는 이미지 삭제
                user.avatarImg?.let { exist ->
                    gcsUtil.removeUnusedImg(exist)
                }

                newImg
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

    @Transactional
    fun deleteUser(id: String) {
        tJwtAuthRepository.deleteByUserId(id)
        tFollowRelationshipRepository.deleteByUserId(id)
        tScheduleMemberRepository.deleteByGroupKeyUserId(id)

        val schedules = tScheduleRepository.findByOwnerId(id)
        val deleteSchedules = mutableListOf<TSchedule>()
        val updatedSchedules = mutableListOf<TSchedule>()
        schedules.map {
            val members = tScheduleMemberRepository.findByGroupKeyGroupId(it.memberGroup.id!!)
            if (members.isEmpty()) {
                deleteSchedules.add(it)
            } else {
                updatedSchedules.add(
                    it.copy(ownerId = members[0].groupKey.user.id)
                )
            }
        }
        tScheduleRepository.deleteAll(deleteSchedules)
        tScheduleRepository.saveAll(updatedSchedules)

        tUserRepository.deleteById(id)
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
