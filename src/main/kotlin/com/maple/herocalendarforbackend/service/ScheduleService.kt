package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleGetRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ScheduleMemberResponse
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Suppress("TooManyFunctions", "MagicNumber")
class ScheduleService(
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tUserRepository: TUserRepository,
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
    fun save(requesterId: String, request: ScheduleAddRequest) {
        val owner =
            tUserRepository.findByIdAndVerified(requesterId, true) ?: throw BaseException(BaseResponseCode.NOT_FOUND)
        val schedule = tScheduleRepository.save(TSchedule.convert(request, requesterId))
        val members = mutableListOf(TScheduleMember.initConvert(owner, schedule, AcceptedStatus.ACCEPTED))

        val searchMember = request.members.filter { it != owner.email }.toSet().toList()
        if (searchMember.isNotEmpty()) {
            val partyMembers = tUserRepository.findByEmailIn(searchMember)
                .map { TScheduleMember.initConvert(it, schedule, AcceptedStatus.WAITING) }
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
    fun delete(scheduleId: Long, requesterId: String) {
        val schedule = findById(scheduleId)
        val user = tScheduleMemberRepository.findByScheduleKeyScheduleId(scheduleId)
            .firstOrNull { m -> m.scheduleKey.user.id == requesterId }
        if (schedule.ownerId == requesterId) {
            tScheduleMemberRepository.deleteByScheduleKeyScheduleId(scheduleId)
            tScheduleRepository.deleteById(scheduleId)
        } else if (user != null) {
            tScheduleMemberRepository.deleteByScheduleKey(
                TScheduleMember.ScheduleKey(schedule, user.scheduleKey.user)
            )
        } else {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
    }

    /**
     * 멤버 추가
     */
    @Transactional
    fun updateMember(requesterId: String, request: ScheduleMemberAddRequest) {
        val schedule = findById(request.scheduleId)
        val members = tScheduleMemberRepository.findByScheduleKeyScheduleId(request.scheduleId)
        if (members.none { m -> m.scheduleKey.user.id == requesterId }) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        val newMembers = request.newMember.toSet().toList().filter { email ->
            members.count { m ->
                m.scheduleKey.user.email == email
            } == 0
        }

        // save new members
        if (newMembers.isNotEmpty()) {
            tUserRepository.findByEmailIn(newMembers)
                .map { user -> TScheduleMember.initConvert(user, schedule, AcceptedStatus.WAITING) }
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
    fun changeOwnerRequest(scheduleId: Long, requesterId: String, nextOwnerEmail: String) {
        val schedule = findById(scheduleId)
        if (schedule.ownerId != requesterId) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        val nextOwner = tUserRepository.findByEmailAndVerified(nextOwnerEmail, true)
            ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        // 스케줄 멤버가 아닌 유저에게 넘기려는 경우
        tScheduleMemberRepository.save(
            TScheduleMember.initConvert(nextOwner, schedule, AcceptedStatus.WAITING)
        )
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
    fun changeOwnerAccept(scheduleId: Long, newOwnerId: String) {
        val schedule = findById(scheduleId)
        if (!schedule.waitingOwnerChange || schedule.nextOwnerId != newOwnerId) {
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
    fun changeOwnerRefuse(scheduleId: Long, newOwnerId: String) {
        val schedule = findById(scheduleId)
        if (!schedule.waitingOwnerChange || schedule.nextOwnerId != newOwnerId) {
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
    fun update(requesterId: String, request: ScheduleUpdateRequest) {
        val schedule = findById(request.scheduleId)
        val members = tScheduleMemberRepository.findByScheduleKeyScheduleId(request.scheduleId)
        if (members.none { m -> m.scheduleKey.user.id == requesterId }) {
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

    /**
     * 스케줄 추가 요청 수락
     */
    @Transactional
    fun scheduleAccept(scheduleId: Long, requesterId: String) {
        tScheduleMemberRepository.findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId, requesterId)?.let {
            if (it.acceptedStatus != AcceptedStatus.ACCEPTED) {
                tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.ACCEPTED))
            }
        } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 추가 요청 거절
     */
    @Transactional
    fun scheduleRefuse(scheduleId: Long, requesterId: String) {
        val schedule = findById(scheduleId)
        tScheduleMemberRepository.findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId, requesterId)?.let {
            if (it.acceptedStatus != AcceptedStatus.REFUSED) {
                tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.REFUSED))
            }
        } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 목록
     */
    fun findSchedules(userId: String, data: ScheduleGetRequest): List<TSchedule> {
        val now = LocalDate.now()
        // todo 한 페이지에 보이는 캘린더 시작일, 종료일로 설정
        val from = data.from ?: LocalDate.of(
            now.year, now.month, 1
        )
        val to = data.to ?: LocalDate.of(
            now.year, now.month, 31
        )
        from.format(DateTimeFormatter.ISO_DATE)
        to.format(DateTimeFormatter.ISO_DATE)

        return tScheduleRepository.findByFromToAndUserId(userId, from, to)
    }

    /**
     * 스케줄 목록(controller 가 호출)
     */
    fun findSchedulesAndConvertToResponse(userId: String, data: ScheduleGetRequest): List<ScheduleResponse> {
        val schedules = findSchedules(userId, data)
        val scheduleGroup = schedules.associateBy { it.id }
        val members = tScheduleMemberRepository.findByScheduleKeyScheduleIdIn(schedules.mapNotNull { it.id })
        val memberGroup: Map<Long?, List<TScheduleMember>> = members.groupBy { it.scheduleKey.schedule.id }
        return memberGroup.mapNotNull { group ->
            val participants = group.value.map { ScheduleMemberResponse.convert(it) }
            scheduleGroup[group.key]?.let {
                ScheduleResponse.convert(
                    data = it,
                    members = participants
                )
            }
        }
    }
}
