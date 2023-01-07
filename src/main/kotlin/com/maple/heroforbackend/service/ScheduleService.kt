package com.maple.heroforbackend.service

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.request.GetSchedulesRequest
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
    fun findById(scheduleId: Long): TSchedule {
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
    fun save(owner: TUser, request: ScheduleAddRequest) {
        val schedule = tScheduleRepository.save(TSchedule.convert(request, owner.id))
        val members = mutableListOf(TScheduleMember.initConvert(owner, schedule, true))

        val searchMember = request.members.filter { it != owner.email }.toSet().toList()
        if (searchMember.isNotEmpty()) {
            val partyMembers = tUserRepository.findByEmailIn(searchMember)
                .map { TScheduleMember.initConvert(it, schedule, false) }
            if (partyMembers.isEmpty()) {
                throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            }
            members.addAll(partyMembers)
        }
        tScheduleMemberRepository.saveAll(members)
    }

    /**
     * 스케줄 삭제
     */
    @Transactional
    fun delete(scheduleId: Long, requestUser: TUser) {
        val schedule = findById(scheduleId)
        if (schedule.ownerId == requestUser.id) {
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
        val schedule = findById(scheduleId)
        if (schedule.members.none { m -> m.user.id == requestUser.id }) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        val newMembers = request.newMember.toSet().toList().filter { email ->
            schedule.members.count { m ->
                m.user.email == email
            } == 0
        }

        // save new members
        if (newMembers.isNotEmpty()) {
            tUserRepository.findByEmailIn(newMembers)
                .map { user -> TScheduleMember.initConvert(user, schedule, false) }
                .let {
                    if (it.isNotEmpty()) {
                        tScheduleMemberRepository.saveAll(it)
                    }
                }
        }
    }

    /**
     * 스케줄 소유자 위임 요청(메일 전송)
     * 스케줄을 소유자 변경 요청 상태로 변경
     */
    @Transactional
    fun changeOwnerRequest(scheduleId: Long, requestUser: TUser, nextOwnerEmail: String) {
        val schedule = findById(scheduleId)
        if (schedule.ownerId != requestUser.id) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        val nextOwner = tUserRepository.findByEmailAndVerified(nextOwnerEmail, true)
            ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        emailSendService.sendOwnerChangeRequestEmail(scheduleId, nextOwner.email)
        tScheduleRepository.save(
            schedule.copy(
                nextOwnerId = nextOwner.id,
                waitingOwnerChange = true
            )
        )
    }

    /**
     * 스케줄 소유자 위임
     * 스케줄을 소유자 변경 요청 상태에서 완료로 변경
     */
    @Transactional
    fun changeOwnerAccept(scheduleId: Long, newOwner: TUser) {
        val schedule = findById(scheduleId)
        if (!schedule.waitingOwnerChange || schedule.nextOwnerId != newOwner.id) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        tScheduleRepository.save(
            schedule.copy(
                ownerId = schedule.nextOwnerId,
                nextOwnerId = null,
                waitingOwnerChange = false,
            )
        )
    }

    /**
     * 스케줄의 소유자 변경 요청을 거절
     */
    @Transactional
    fun changeOwnerRefuse(scheduleId: Long, newOwner: TUser) {
        val schedule = findById(scheduleId)
        if (!schedule.waitingOwnerChange || schedule.nextOwnerId != newOwner.id) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        tScheduleRepository.save(
            schedule.copy(
                nextOwnerId = null,
                waitingOwnerChange = false
            )
        )
    }

    /**
     * 스케줄 갱신
     */
    @Transactional
    fun update(scheduleId: Long, requestUser: TUser, request: ScheduleUpdateRequest) {
        val schedule = findById(scheduleId)
        if (schedule.members.none { m -> m.user.id == requestUser.id }) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        tScheduleRepository.save(
            schedule.copy(
                title = request.title,
                start = request.start,
                end = request.end,
                allDay = request.allDay,
                note = request.note,
                isPublic = request.isPublic
            )
        )
    }

//    /**
//     * 스케줄 검색
//     */
//    fun findSchedules(request: GetSchedulesRequest) {
//        // email 검색: 전체 검색(우선 검색)
//        // nickName 검색: 친구 검색
//    }
}
