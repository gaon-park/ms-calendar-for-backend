package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.RepeatCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.entity.TScheduleGroup
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TScheduleGroupRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.ChronoUnit

@Service
@Suppress("TooManyFunctions", "MagicNumber")
class ScheduleService(
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tUserRepository: TUserRepository,
    private val tScheduleGroupRepository: TScheduleGroupRepository,
) {

    fun findById(scheduleId: Long): TSchedule {
        tScheduleRepository.findById(scheduleId).let {
            if (it.isPresent)
                return it.get()
        }
        throw BaseException(BaseResponseCode.NOT_FOUND)
    }

    fun findUserById(id: String): TUser {
        return tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }
    }

    @Transactional
    fun save(requesterId: String, request: ScheduleAddRequest) {
        val owner = findUserById(requesterId)
        tScheduleGroupRepository.save(TScheduleGroup()).let {
            saveSchedule(request, requesterId, it)
            saveScheduleMember(owner, request.memberIds, it)
        }
    }

    @Transactional
    fun saveScheduleMember(owner: TUser, memberIds: List<String>, group: TScheduleGroup) {
        val members =
            tUserRepository.findPublicOrFriendByIdIn(memberIds.filter { it != owner.id }.toSet().toList(), owner.id!!)
        val memberData = mutableListOf(
            TScheduleMember.initConvert(
                owner, group, AcceptedStatus.ACCEPTED
            )
        )
        memberData.addAll(members.map {
            TScheduleMember.initConvert(
                it, group, AcceptedStatus.WAITING
            )
        })

        tScheduleMemberRepository.saveAll(memberData)
    }

    @Transactional
    fun saveSchedule(
        request: ScheduleAddRequest, ownerId: String, group: TScheduleGroup
    ) {
        val requestStart = request.start
        val diff = Duration.between(requestStart, request.end).toMinutes()
        val start = requestStart.toLocalDate()
        val end = when {
            (request.repeatInfo == null) -> request.end.toLocalDate()
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
            val tempStart = LocalDateTime.of(
                it.year, it.month, it.dayOfMonth, requestStart.hour, requestStart.minute
            )
            TSchedule.convert(
                request = request,
                ownerId = ownerId,
                group = group,
                start = tempStart,
                end = tempStart.plusMinutes(diff)
            )
        }.toList())

        tScheduleRepository.saveAll(repeatSchedules)
    }

    /**
     * 멤버 추가
     */
    @Transactional
    fun updateMember(requesterId: String, request: ScheduleMemberAddRequest) {
        val schedule = findById(request.scheduleId)
        schedule.group.id?.let {
            tScheduleMemberRepository.findByGroupKeyGroupId(it).firstOrNull { m ->
                m.groupKey.user.id == requesterId
            } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        tScheduleMemberRepository.saveAll(
            tUserRepository.findPublicOrFriendByIdIn(request.newMemberIds, requesterId)
                .map {
                    TScheduleMember.initConvert(it, schedule.group, AcceptedStatus.WAITING)
                }
        )
    }

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
