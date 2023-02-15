package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ProfileRequest
import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.entity.IProfile
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFollowRepository
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
    private val tFollowRepository: TFollowRepository,
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails? = username?.let {
        tUserRepository.findByEmail(it)
    }

    fun findByAccountIdToIProfile(accountId: String, loginUserId: String?): IProfile? =
        tUserRepository.findByAccountIdToIProfile(accountId, loginUserId ?: "")

    fun findByConditionAndUserId(request: SearchUserRequest, loginUserId: String?) =
        tUserRepository.findByConditionAndUserId(
            keyword = if (request.keyword != null) "%${request.keyword}%" else "",
            world = request.world ?: "",
            job = request.job ?: "",
            jobDetail = request.jobDetail ?: "",
            loginUserId = loginUserId ?: ""
        )

    fun findByConditionCount(request: SearchUserRequest) = tUserRepository.findByConditionCount(
        keyword = if (request.keyword != null) "%${request.keyword}%" else "",
        world = request.world ?: "",
        job = request.job ?: "",
        jobDetail = request.jobDetail ?: "",
    )

    fun findById(id: String): TUser =
        tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }

    fun findByIdToIProfileResponse(loginUserId: String): IProfileResponse {
        tUserRepository.findByIdToIProfile(loginUserId)?.let {
            return IProfileResponse(
                profile = it,
                follow = tFollowRepository.findAllStatusFollowByUserId(loginUserId),
                follower = tFollowRepository.findAllStatusFollowerByUserId(loginUserId),
                acceptedFollowCount = tFollowRepository.findCountJustAcceptedFollowByUserId(loginUserId),
                acceptedFollowerCount = tFollowRepository.findCountJustAcceptedFollowerByUserId(loginUserId)
            )
        } ?: throw BaseException(BaseResponseCode.USER_NOT_FOUND)
    }

    @Transactional
    fun updateProfile(id: String, request: ProfileRequest): IProfileResponse {
        val user = findByIdToIProfileResponse(id)
        val avatarImg = request.avatarImg?.let {
            if (it.isNotEmpty() && it != user.profile.getAvatarImg()) {
                val gcsUtil = GCSUtil()
                val newImg = gcsUtil.upload(
                    id,
                    ImageUtil().toByteArray(request.avatarImg)
                )

                // 이미지 저장 후, 사용하지 않는 이미지 삭제
                user.profile.getAvatarImg()?.let { exist ->
                    gcsUtil.removeUnusedImg(exist)
                }

                newImg
            } else null
        }
        if (user.profile.getAccountId() != request.accountId && !accountIdDuplicateCheck(request.accountId)) {
            throw BaseException(BaseResponseCode.DUPLICATED_ACCOUNT_ID)
        }

        return if (diffCheck(user.profile, request) || avatarImg != null) {
            tUserRepository.save(
                TUser.updateModel(user.profile, request, avatarImg)
            )
            IProfileResponse(
                profile = tUserRepository.findByIdToIProfile(id) ?: throw BaseException(BaseResponseCode.DATA_ERROR),
                follow = user.follow,
                follower = user.follower,
                acceptedFollowCount = user.acceptedFollowCount,
                acceptedFollowerCount = user.acceptedFollowerCount
            )
        } else user
    }

    @Transactional
    fun deleteUser(id: String) {
        tJwtAuthRepository.deleteByDeletedAccount(id)
        tFollowRepository.deleteByAccountRemove(id)
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

    private fun diffCheck(profile: IProfile, request: ProfileRequest): Boolean {
        return when {
            request.nickName.isNotEmpty() && profile.getNickName() != request.nickName -> true
            request.accountId.isNotEmpty() && profile.getAccountId() != request.accountId -> true
            request.world != profile.getWorld() -> true
            request.job != profile.getJob() -> true
            request.jobDetail != profile.getJobDetail() -> true
            profile.getIsPublic() != request.isPublic -> true
            profile.getNotificationFlg() != request.notificationFlg -> true

            else -> false
        }
    }

    private fun accountIdDuplicateCheck(accountId: String): Boolean =
        tUserRepository.findByAccountId(accountId) == null
}
