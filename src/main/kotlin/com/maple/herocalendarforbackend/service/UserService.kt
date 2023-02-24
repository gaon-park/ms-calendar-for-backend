package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.user.ProfileRequest
import com.maple.herocalendarforbackend.dto.request.search.SearchUserRequest
import com.maple.herocalendarforbackend.dto.response.IProfileResponse
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryBatchRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
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

@Suppress("LongParameterList", "TooManyFunctions")
@Service
class UserService(
    private val tUserRepository: TUserRepository,
    private val tFollowRepository: TFollowRepository,
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeHistoryBatchRepository: TCubeHistoryBatchRepository
) : UserDetailsService {
    private val profileBucketName = "ms-hero-profile"

    override fun loadUserByUsername(username: String?): UserDetails? = username?.let {
        tUserRepository.findByEmail(it)
    }

    fun findUserListForScheduleSearch(keyword: String?, loginUserId: String?): List<TUser> =
        tUserRepository.findUserListForScheduleSearch(
            keyword = if (keyword != null) "%$keyword%" else "",
            loginUserId = loginUserId ?: ""
        )

    fun findByConditionAndUserId(request: SearchUserRequest, loginUserId: String?) =
        tUserRepository.findByConditionAndUserId(
            keyword = if (request.keyword != null) "%${request.keyword}%" else "",
            world = request.world ?: "",
            job = request.job ?: "",
            jobDetail = request.jobDetail ?: "",
            loginUserId = loginUserId ?: ""
        )

    fun findById(id: String): TUser =
        tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }

    fun findByAccountIdToIProfileResponse(accountId: String, loginUserId: String?): IProfileResponse? {
        return tUserRepository.findByAccountIdToIProfile(accountId, loginUserId ?: "")?.let {
            IProfileResponse(
                profile = it,
                follow = if (it.getId() == loginUserId || it.getIsPublic() || it.getIamFollowHim() == "FOLLOW")
                    tFollowRepository.findAllStatusFollowByUserId(
                        it.getId()
                    ) else emptyList(),
                follower = if (it.getId() == loginUserId || it.getIsPublic() || it.getIamFollowHim() == "FOLLOW")
                    tFollowRepository.findAllStatusFollowerByUserId(
                        it.getId()
                    )
                else emptyList(),
                acceptedFollowCount = tFollowRepository.findCountJustAcceptedFollowByUserId(it.getId()),
                acceptedFollowerCount = tFollowRepository.findCountJustAcceptedFollowerByUserId(it.getId())
            )
        }
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
        val user = findById(id)
        val avatarImg = request.avatarImg?.let {
            if (it.isNotEmpty() && it != user.avatarImg) {
                val gcsUtil = GCSUtil()
                val newImg = gcsUtil.upload(
                    id,
                    ImageUtil().toByteArray(request.avatarImg),
                    profileBucketName
                )

                // 이미지 저장 후, 사용하지 않는 이미지 삭제
                user.avatarImg?.let { exist ->
                    gcsUtil.removeUnusedImg(exist, profileBucketName)
                }

                newImg
            } else null
        }
        if (user.accountId != request.accountId && !accountIdDuplicateCheck(request.accountId)) {
            throw BaseException(BaseResponseCode.DUPLICATED_ACCOUNT_ID)
        }

        if (diffCheck(user, request) || avatarImg != null) {
            tUserRepository.save(
                TUser.updateModel(user, request, avatarImg)
            )
        }
        return findByIdToIProfileResponse(id)
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
                    it.copy(owner = members[0].groupKey.user)
                )
            }
        }
        tScheduleRepository.deleteAll(deleteSchedules)
        tScheduleRepository.saveAll(updatedSchedules)

        tCubeApiKeyRepository.deleteByAccount(id)
        tCubeHistoryRepository.deleteByAccount(id)
        tCubeHistoryBatchRepository.deleteByAccount(id)

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
            user.notificationFlg != request.notificationFlg -> true

            else -> false
        }
    }

    private fun accountIdDuplicateCheck(accountId: String): Boolean =
        tUserRepository.findByAccountId(accountId) == null
}
