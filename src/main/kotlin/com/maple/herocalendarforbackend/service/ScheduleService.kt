package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.RepeatCode
import com.maple.herocalendarforbackend.code.ScheduleUpdateCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleDeleteRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ScheduleMemberResponse
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.entity.TScheduleMemberGroup
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TScheduleMemberGroupRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

@Service
@Suppress("TooManyFunctions", "MagicNumber")
class ScheduleService(
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tUserRepository: TUserRepository,
    private val tScheduleMemberGroupRepository: TScheduleMemberGroupRepository,
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
        tScheduleMemberGroupRepository.save(TScheduleMemberGroup()).let {
            saveSchedule(request, requesterId, it)
            saveScheduleMember(owner, request.memberIds, it)
        }
    }

    @Transactional
    fun saveScheduleMember(owner: TUser, memberIds: List<String>, group: TScheduleMemberGroup) {
        val members =
            tUserRepository.findPublicOrFollowing(memberIds.filter { it != owner.id }.toSet().toList(), owner.id!!)
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
    fun saveSchedule(request: ScheduleAddRequest, ownerId: String, memberGroup: TScheduleMemberGroup) {
        val requestStart = request.start
        val diff = Duration.between(requestStart, request.end).toMinutes()
        val start = requestStart.toLocalDate()
        val end = when {
            (request.repeatInfo == null) -> request.end.toLocalDate()
            (request.repeatInfo.end == null) -> LocalDate.of(start.year + 1, start.month, start.dayOfMonth)
            else -> request.repeatInfo.end
        }
        val period = when (request.repeatInfo?.repeatCode) {
            RepeatCode.DAYS -> Period.ofDays(1)
            RepeatCode.WEEKS -> Period.ofWeeks(1)
            RepeatCode.MONTHS -> Period.ofMonths(1)
            RepeatCode.YEARS -> Period.ofYears(1)
            else -> Period.ofDays(1)
        }

        val parentSchedule = tScheduleRepository.save(TSchedule.convert(request, ownerId, memberGroup))
        val repeatSchedules = mutableListOf(parentSchedule.copy(parentId = parentSchedule.id))
        repeatSchedules.addAll(start.datesUntil(end.plusDays(1), period).skip(1).map {
            val tempStart = LocalDateTime.of(
                it.year, it.month, it.dayOfMonth, requestStart.hour, requestStart.minute
            )
            TSchedule.convert(
                request = request,
                schedule = parentSchedule,
                start = tempStart,
                end = tempStart.plusMinutes(diff)
            )
        }.toList())

        tScheduleRepository.saveAll(repeatSchedules)
    }

    /**
     * 멤버 추가
     * // todo 차후 멤버 삭제도 가능하게
     */
    @Transactional
    fun updateMember(requesterId: String, request: ScheduleMemberAddRequest) {
        val schedule = findById(request.scheduleId)
        val alreadyMemberIds = tScheduleMemberRepository.findByGroupKeyGroupId(schedule.memberGroup.id!!)
            .mapNotNull { it.groupKey.user.id }
        val inviteMembers = request.newMemberIds.filter {
            !alreadyMemberIds.contains(it)
        }
        if (inviteMembers.isNotEmpty()) {
            tScheduleMemberRepository.saveAll(
                tUserRepository.findPublicOrFollowing(request.newMemberIds, requesterId)
                    .map {
                        TScheduleMember.initConvert(it, schedule.memberGroup, AcceptedStatus.WAITING)
                    }
            )
        }
    }

    @Transactional
    fun update(requesterId: String, request: ScheduleUpdateRequest) {
        val schedule = findById(request.scheduleId)
        schedule.memberGroup.id?.let {
            tScheduleMemberRepository.findByGroupKeyGroupId(it).firstOrNull { m ->
                m.groupKey.user.id == requesterId
            } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        }
        // 업데이트 사항이 있을 때만
        if (updateDataCompare(schedule, request)) {
            val entities = when (request.scheduleUpdateCode) {
                ScheduleUpdateCode.ALL -> tScheduleRepository.findByParentId(schedule.parentId)
                ScheduleUpdateCode.ONLY_THIS -> listOf(schedule)
                ScheduleUpdateCode.THIS_AND_FUTURE -> tScheduleRepository.findByGroupAndAfterDay(
                    schedule.memberGroup.id,
                    schedule.start
                )
            }

            val startDiff = Duration.between(schedule.start, request.start).toMinutes()
            val endDiff = Duration.between(schedule.end, request.end).toMinutes()
            tScheduleRepository.saveAll(
                entities.map {
                    it.copy(
                        title = request.title,
                        start = it.start.plusMinutes(startDiff),
                        end = it.end.plusMinutes(endDiff),
                        allDay = request.allDay,
                        isPublic = request.isPublic
                    )
                }
            )
        }
    }

    fun updateDataCompare(schedule: TSchedule, request: ScheduleUpdateRequest): Boolean =
        when {
            schedule.title != request.title -> true
            schedule.start != request.start -> true
            schedule.end != request.end -> true
            schedule.allDay != request.allDay -> true
            schedule.isPublic != request.isPublic -> true
            else -> false
        }

    /**
     * 스케줄 추가 요청 수락
     */
    @Transactional
    fun inviteAccept(scheduleId: Long, requesterId: String) {
        tScheduleMemberRepository.findByScheduleIdAndUserIdAndAcceptedStatus(
            scheduleId,
            requesterId,
            AcceptedStatus.ACCEPTED.toString()
        )
            ?.let {
                tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.ACCEPTED))
            } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 추가 요청 거절
     */
    @Transactional
    fun inviteRefuse(scheduleId: Long, requesterId: String) {
        tScheduleMemberRepository.findByScheduleIdAndUserIdAndAcceptedStatus(
            scheduleId,
            requesterId,
            AcceptedStatus.REFUSED.toString()
        )?.let {
            tScheduleMemberRepository.save(it.copy(acceptedStatus = AcceptedStatus.REFUSED))
        } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 로그인 유저의 스케줄 검색
     */
    fun findForPersonal(loginUserId: String, from: LocalDate?, to: LocalDate?): List<ScheduleResponse> {
        val now = LocalDate.now()
        val fromV = from ?: LocalDate.of(
            now.year, now.month, 1
        )
        val toV = to ?: LocalDate.of(
            now.year, now.month, 31
        )
        fromV.format(DateTimeFormatter.ISO_DATE)
        toV.format(DateTimeFormatter.ISO_DATE)

        return convertToResponse(tScheduleRepository.findByFromToAndUserId(loginUserId, fromV, toV))
    }

    fun findForPublic(
        loginUserId: String?,
        searchUserId: String,
        from: LocalDate?,
        to: LocalDate?
    ): List<ScheduleResponse> {
        return findForPersonal(searchUserId, from, to).mapNotNull {
            val memberIds = it.members.map { m -> m.id }
            if (!it.isPublic && !memberIds.contains(loginUserId)) {
                null
            } else {
                it
            }
        }
    }

    fun convertToResponse(schedules: List<TSchedule>): List<ScheduleResponse> {
        return if (schedules.isNotEmpty()) {
            schedules.map {
                ScheduleResponse.convert(
                    data = it,
                    members = tScheduleMemberRepository.findByGroupKeyGroupId(it.memberGroup.id!!)
                        .map { m -> ScheduleMemberResponse.convert(m) }
                )
            }
        } else emptyList()
    }

    @Transactional
    fun delete(requesterId: String, request: ScheduleDeleteRequest) {
        val schedule = findById(request.scheduleId)
        val members = tScheduleMemberRepository.findByGroupKeyGroupId(schedule.memberGroup.id!!)
        members.firstOrNull { it.groupKey.user.id == requesterId } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        if (schedule.ownerId == requesterId) {
            deleteScheduleByOwner(schedule, request.scheduleUpdateCode)
        } else {
            deleteScheduleByMember(requesterId, schedule, members, request.scheduleUpdateCode)
        }
    }

    /**
     * 요청자가 소유자인 경우, 스케줄 삭제
     */
    @Transactional
    fun deleteScheduleByOwner(schedule: TSchedule, scheduleUpdateCode: ScheduleUpdateCode) {
        val entities = when (scheduleUpdateCode) {
            ScheduleUpdateCode.ALL -> tScheduleRepository.findByParentId(schedule.parentId)
            ScheduleUpdateCode.ONLY_THIS -> listOf(schedule)
            ScheduleUpdateCode.THIS_AND_FUTURE -> tScheduleRepository.findByGroupAndAfterDay(
                schedule.memberGroup.id,
                schedule.start
            )
        }

        entities.mapNotNull { it.memberGroup.id }.toSet().toList()
        tScheduleRepository.deleteAll(entities)
    }

    /**
     * 요청자가 참석자인 경우, 스케줄 참석자 명단에서 거절로 변경
     */
    @Transactional
    fun deleteScheduleByMember(
        requesterId: String,
        schedule: TSchedule,
        members: List<TScheduleMember>,
        scheduleUpdateCode: ScheduleUpdateCode
    ) {
        val entities = when (scheduleUpdateCode) {
            ScheduleUpdateCode.ALL -> tScheduleRepository.findByParentId(schedule.parentId)
            ScheduleUpdateCode.ONLY_THIS -> listOf(schedule)
            ScheduleUpdateCode.THIS_AND_FUTURE -> tScheduleRepository.findByGroupAndAfterDay(
                schedule.memberGroup.id,
                schedule.start
            )
        }

        tScheduleMemberGroupRepository.save(TScheduleMemberGroup()).let {
            tScheduleRepository.saveAll(entities.map { entity -> entity.copy(memberGroup = it) })
            val newMemberData = members.map { m ->
                if (m.groupKey.user.id == requesterId) {
                    TScheduleMember.initConvert(
                        user = m.groupKey.user,
                        group = it,
                        acceptedStatus = AcceptedStatus.REFUSED
                    )
                } else {
                    TScheduleMember.initConvert(
                        user = m.groupKey.user,
                        group = it,
                        acceptedStatus = m.acceptedStatus
                    )
                }
            }
            tScheduleMemberRepository.saveAll(newMemberData)
        }
    }
}
