package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.RepeatCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.entity.TScheduleMemberGroup
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TScheduleMemberGroupRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TScheduleOwnerRequestRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Service
@Suppress("TooManyFunctions", "MagicNumber")
class ScheduleService(
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tUserRepository: TUserRepository,
    private val tScheduleOwnerRequestRepository: TScheduleOwnerRequestRepository,
    private val tScheduleMemberGroupRepository: TScheduleMemberGroupRepository,
) {
    fun findUserById(id: String): TUser {
        return tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }
    }

    @Transactional
    fun save(requesterId: String, request: ScheduleAddRequest) {
        val owner = findUserById(requesterId)
        tScheduleMemberGroupRepository.save(TScheduleMemberGroup()).let {
            saveSchedule(request, requesterId, it)
            saveScheduleMember(owner, request.memberIds, it)
        }
    }

    @Transactional
    fun saveScheduleMember(owner: TUser, memberIds: List<String>, memberGroup: TScheduleMemberGroup) {
        val members =
            tUserRepository.findPublicOrFriendByIdIn(memberIds.filter { it != owner.id }.toSet().toList(), owner.id!!)
        val memberData = mutableListOf(
            TScheduleMember.initConvert(
                owner, memberGroup, AcceptedStatus.ACCEPTED
            )
        )
        memberData.addAll(members.map {
            TScheduleMember.initConvert(
                it, memberGroup, AcceptedStatus.WAITING
            )
        })

        tScheduleMemberRepository.saveAll(memberData)
    }

    @Transactional
    fun saveSchedule(
        request: ScheduleAddRequest, ownerId: String, memberGroup: TScheduleMemberGroup
    ) {
        val requestStart = request.start
        val requestEnd = request.end
        val start = requestStart.toLocalDate()
        val end = when {
            (request.repeatInfo == null) -> requestEnd.toLocalDate()
            (request.repeatInfo.end == null) -> LocalDate.of(start.year + 2, start.month, start.dayOfMonth)
            else -> request.repeatInfo.end
        }
        val period = when (request.repeatInfo?.repeatCode) {
            RepeatCode.DAYS -> Period.ofDays(1)
            RepeatCode.WEEKS -> Period.ofWeeks(1)
            RepeatCode.MONTHS -> Period.ofMonths(1)
            RepeatCode.YEARS -> Period.ofYears(1)
            else -> Period.ofDays(1)
        }
        val repeatSchedules = mutableListOf<TSchedule>()
        repeatSchedules.addAll(start.datesUntil(end.plusDays(1), period).map {
            TSchedule.convert(
                request = request, ownerId = ownerId, memberGroup = memberGroup, start = LocalDateTime.of(
                    it.year, it.month, it.dayOfMonth, requestStart.hour, requestStart.minute
                ), end = LocalDateTime.of(
                    it.year, it.month, it.dayOfMonth, requestEnd.hour, requestEnd.minute
                )
            )
        }.toList())

        tScheduleRepository.saveAll(repeatSchedules)
    }

//
//    fun findById(scheduleId: Long): TSchedule {
//        tScheduleRepository.findById(scheduleId).let {
//            if (it.isPresent)
//                return it.get()
//        }
//        throw BaseException(BaseResponseCode.NOT_FOUND)
//    }
//
//    /**
//     * 스케줄 입력
//     */
//    @Transactional
//    fun save(requesterId: String, request: ScheduleAddRequest) {
//        val owner = findUserById(requesterId)
//        val schedule = tScheduleRepository.save(TSchedule.convert(request, requesterId))
//        val members = mutableListOf(TScheduleMember.initConvert(owner, schedule, AcceptedStatus.ACCEPTED))
//        val searchMember = request.memberIds.filter { it != owner.id }.toSet().toList()
//        if (searchMember.isNotEmpty()) {
//            val partyMembers = tUserRepository.findByIdIn(searchMember)
//                .map { TScheduleMember.initConvert(it, schedule, AcceptedStatus.WAITING) }
//            if (partyMembers.isNotEmpty()) {
//                members.addAll(partyMembers)
//            }
//        }
//        tScheduleMemberRepository.saveAll(members)
//    }
//
//    /**
//     * 스케줄 삭제
//     */
//    @Transactional
//    fun delete(scheduleId: Long, requesterId: String) {
//        val schedule = findById(scheduleId)
//        val user = tScheduleMemberRepository.findByScheduleKeyScheduleId(scheduleId)
//            .firstOrNull { m -> m.scheduleKey.user.id == requesterId }
//        if (schedule.ownerId == requesterId) {
//            tScheduleMemberRepository.deleteByScheduleKeyScheduleId(scheduleId)
//            tScheduleRepository.deleteById(scheduleId)
//        } else if (user != null) {
//            tScheduleMemberRepository.deleteByScheduleKey(
//                TScheduleMember.ScheduleKey(schedule, user.scheduleKey.user)
//            )
//        } else {
//            throw BaseException(BaseResponseCode.BAD_REQUEST)
//        }
//    }
//
//    /**
//     * 멤버 추가
//     */
//    @Transactional
//    fun updateMember(requesterId: String, request: ScheduleMemberAddRequest) {
//        val schedule = findById(request.scheduleId)
//        val members = tScheduleMemberRepository.findByScheduleKeyScheduleId(request.scheduleId)
//        if (members.none { m -> m.scheduleKey.user.id == requesterId }) {
//            throw BaseException(BaseResponseCode.BAD_REQUEST)
//        }
//        val newMembers = request.newMemberIds.toSet().toList().filter { id ->
//            members.count { m ->
//                m.scheduleKey.user.id == id
//            } == 0
//        }
//
//        // save new members
//        if (newMembers.isNotEmpty()) {
//            tUserRepository.findByIdIn(newMembers)
//                .map { user -> TScheduleMember.initConvert(user, schedule, AcceptedStatus.WAITING) }
//                .let {
//                    if (it.isNotEmpty()) {
//                        tScheduleMemberRepository.saveAll(it)
//                    }
//                }
//        }
//    }
//
//    /**
//     * 스케줄 소유자 위임 요청
//     * 스케줄을 소유자 변경 요청 상태로 변경
//     */
//    @Transactional
//    fun changeOwnerRequest(requesterId: String, request: ScheduleOwnerChangeRequest) {
//        val requester = findUserById(requesterId)
//        val nextOwner = findUserById(request.nextOwnerId)
//        val schedule = findById(request.scheduleId)
//        if (schedule.ownerId != requesterId) {
//            throw BaseException(BaseResponseCode.BAD_REQUEST)
//        }
//
//        tScheduleOwnerRequestRepository.findById(
//            TScheduleOwnerRequest.OwnerRequestId(schedule, requester)
//        ).let {
//            if (it.isPresent) {
//                throw BaseException(BaseResponseCode.WAITING_FOR_RESPONDENT)
//            }
//        }
//
//        // 스케줄 멤버가 아닌 유저에게 넘기려는 경우
//        tScheduleMemberRepository.save(
//            TScheduleMember.initConvert(nextOwner, schedule, AcceptedStatus.WAITING)
//        )
//
//        tScheduleOwnerRequestRepository.save(
//            TScheduleOwnerRequest.convert(schedule, requester, nextOwner)
//        )
//    }
//
//    /**
//     * 스케줄 소유자 위임
//     * 스케줄을 소유자 변경 요청 상태에서 완료로 변경
//     */
//    @Transactional
//    fun changeOwnerAccept(scheduleId: Long, newOwnerId: String) {
//        val request = tScheduleOwnerRequestRepository.findRequest(
//            scheduleId, newOwnerId
//        ) ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
//        tScheduleRepository.save(
//            request.requestId.schedule.copy(ownerId = newOwnerId)
//        )
//        tScheduleMemberRepository.findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId, newOwnerId)?.let {
//            if (it.acceptedStatus != AcceptedStatus.ACCEPTED) {
//                tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.ACCEPTED))
//            }
//        }
//        tScheduleOwnerRequestRepository.delete(request)
//    }
//
//    /**
//     * 스케줄의 소유자 변경 요청을 거절
//     */
//    @Transactional
//    fun changeOwnerRefuse(scheduleId: Long, newOwnerId: String) {
//        val request = tScheduleOwnerRequestRepository.findRequest(
//            scheduleId, newOwnerId
//        ) ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
//        tScheduleOwnerRequestRepository.delete(request)
//    }
//
//    /**
//     * 스케줄 갱신
//     */
//    @Transactional
//    fun update(requesterId: String, request: ScheduleUpdateRequest) {
//        val schedule = findById(request.scheduleId)
//        val members = tScheduleMemberRepository.findByScheduleKeyScheduleId(request.scheduleId)
//        if (members.none { m -> m.scheduleKey.user.id == requesterId }) {
//            throw BaseException(BaseResponseCode.BAD_REQUEST)
//        }
//
//        tScheduleRepository.save(
//            schedule.copy(
//                title = request.title,
//                start = request.start,
//                end = request.end,
//                allDay = request.allDay,
//                note = request.note,
//            )
//        )
//    }
//
//    /**
//     * 스케줄 추가 요청 수락
//     */
//    @Transactional
//    fun scheduleAccept(scheduleId: Long, requesterId: String) {
//        tScheduleMemberRepository.findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId, requesterId)?.let {
//            if (it.acceptedStatus != AcceptedStatus.ACCEPTED) {
//                tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.ACCEPTED))
//            }
//        } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
//    }
//
//    /**
//     * 스케줄 추가 요청 거절
//     */
//    @Transactional
//    fun scheduleRefuse(scheduleId: Long, requesterId: String) {
//        findById(scheduleId)
//        tScheduleMemberRepository.findByScheduleKeyScheduleIdAndScheduleKeyUserId(scheduleId, requesterId)?.let {
//            if (it.acceptedStatus != AcceptedStatus.REFUSED) {
//                tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.REFUSED))
//            }
//        } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
//    }
//
//    /**
//     * 스케줄 목록
//     */
//    fun findSchedules(userId: String, from: LocalDate?, to: LocalDate?): List<TSchedule> {
//        val now = LocalDate.now()
//        val fromV = from ?: LocalDate.of(
//            now.year, now.month, 1
//        )
//        val toV = to ?: LocalDate.of(
//            now.year, now.month, 31
//        )
//        fromV.format(DateTimeFormatter.ISO_DATE)
//        toV.format(DateTimeFormatter.ISO_DATE)
//
//        return tScheduleRepository.findByFromToAndUserId(userId, fromV, toV)
//    }
//
//    /**
//     * 스케줄 목록(controller 가 호출)
//     */
//    fun findSchedulesAndConvertToResponse(userId: String, from: LocalDate?, to: LocalDate?): List<ScheduleResponse> {
//        val schedules = findSchedules(userId, from, to)
//        val scheduleGroup = schedules.associateBy { it.id }
//        val members = tScheduleMemberRepository.findByScheduleKeyScheduleIdIn(schedules.mapNotNull { it.id })
//        val memberGroup: Map<Long?, List<TScheduleMember>> = members.groupBy { it.scheduleKey.schedule.id }
//        return memberGroup.mapNotNull { group ->
//            val participants = group.value.map { ScheduleMemberResponse.convert(it) }
//            scheduleGroup[group.key]?.let {
//                ScheduleResponse.convert(
//                    data = it,
//                    members = participants
//                )
//            }
//        }
//    }
}
