package com.maple.heroforbackend.service

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.request.ScheduleAddRequest
import com.maple.heroforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.heroforbackend.dto.request.ScheduleUpdateRequest
import com.maple.heroforbackend.entity.TSchedule
import com.maple.heroforbackend.entity.TScheduleMember
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.repository.TScheduleMemberRepository
import com.maple.heroforbackend.repository.TScheduleRepository
import com.maple.heroforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScheduleService(
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tUserRepository: TUserRepository,
    private val emailSendService: EmailSendService,
) {
    fun selectSchedule(scheduleId: Long): TSchedule {
        tScheduleRepository.findById(scheduleId).let {
            if (it.isPresent)
                return it.get()
        }
        throw BaseException(BaseResponseCode.NOT_FOUND)
    }

    /**
     * 스케줄 입력
     */
    @Transactional
    fun insert(owner: TUser, request: ScheduleAddRequest) {
        val schedule = tScheduleRepository.save(TSchedule.convert(request))
        val members = listOf(TScheduleMember.initConvert(owner, true, schedule))
            .plus(
                tUserRepository.findByEmailIn(request.members)
                    .map { TScheduleMember.initConvert(it, false, schedule) }
            )
        tScheduleMemberRepository.saveAll(members)
    }

    /**
     * 스케줄 삭제
     */
    @Transactional
    fun delete(scheduleId: Long, requestUser: TUser) {
        val schedule = selectSchedule(scheduleId)
        if (schedule.members.firstOrNull { m -> m.isOwner && m.user.id == requestUser.id } != null) {
            tScheduleMemberRepository.deleteByScheduleId(scheduleId)
            tScheduleRepository.deleteById(scheduleId)
        } else if (schedule.members.firstOrNull { m -> m.user.id == requestUser.id } != null) {
            tScheduleMemberRepository.deleteByScheduleIdAndUserId(scheduleId, requestUser.id!!)
        } else {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
    }

    /**
     * 멤버 추가
     */
    @Transactional
    fun updateMember(scheduleId: Long, requestUser: TUser, request: ScheduleMemberAddRequest) {
        val schedule = selectSchedule(scheduleId)
        if (schedule.members.none { m -> m.user.id == requestUser.id }) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        // save new members
        tUserRepository.findByEmailIn(request.newMember)
            .map { user -> TScheduleMember.initConvert(user, false, schedule) }
            .let {
                if (it.isNotEmpty()) {
                    tScheduleMemberRepository.saveAll(it)
                }
            }
    }

    /**
     * 스케줄 소유자 위임 요청(메일 전송)
     * 스케줄을 소유자 변경 요청 상태로 변경
     */
    @Transactional
    fun changeOwnerRequest(scheduleId: Long, requestUser: TUser, nextOwnerEmail: String) {
        val schedule = selectSchedule(scheduleId)
        if (schedule.members.none { it.isOwner && it.user.id == requestUser.id }) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        val nextOwner =
            tUserRepository.findByEmail(nextOwnerEmail) ?: throw BaseException(BaseResponseCode.USER_NOT_FOUND)
        emailSendService.sendOwnerChangeRequestEmail(scheduleId, nextOwner.email)
        changeJustOwnerRequestStatus(schedule, true, nextOwner.id)
    }

    /**
     * 스케줄 소유자 위임
     * 스케줄을 소유자 변경 요청 상태에서 완료로 변경
     */
    @Transactional
    fun changeOwner(scheduleId: Long, newOwner: TUser) {
        val schedule = selectSchedule(scheduleId)
        if (!schedule.waitingOwnerChange || schedule.nextOwnerId != newOwner.id) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        tScheduleMemberRepository.saveAll(
            schedule.members.map {
                when {
                    (it.user.id != newOwner.id && it.isOwner) -> it.copy(isOwner = false)
                    (it.user.id == newOwner.id) -> it.copy(isOwner = true)
                    else -> it
                }
            }
        )
        changeJustOwnerRequestStatus(schedule, false, null)
    }

    /**
     * 스케줄의 소유자 변경 요청을 거절
     */
    @Transactional
    fun changeOwnerRefuse(scheduleId: Long, newOwner: TUser) {
        val schedule = selectSchedule(scheduleId)
        if (!schedule.waitingOwnerChange || schedule.nextOwnerId != newOwner.id) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        changeJustOwnerRequestStatus(schedule, false, null)
    }

    /**
     * 스케줄의 소유자 변경 요청 상태를 변경
     */
    @Transactional
    fun changeJustOwnerRequestStatus(schedule: TSchedule, status: Boolean, nextOwnerId: Long?) {
        tScheduleRepository.save(
            schedule.copy(waitingOwnerChange = status, nextOwnerId = nextOwnerId)
        )
    }

    /**
     * 스케줄 갱신
     */
    @Transactional
    fun update(scheduleId: Long, requestUser: TUser, request: ScheduleUpdateRequest) {
        val schedule = selectSchedule(scheduleId)
        if (schedule.members.none { m -> m.user.id == requestUser.id }) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        tScheduleRepository.save(TSchedule.convert(scheduleId, request))
    }
}
