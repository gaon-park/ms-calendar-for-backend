package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.ScheduleAcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.RepeatCode
import com.maple.herocalendarforbackend.code.ScheduleUpdateCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleDeleteRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ScheduleMemberResponse
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.entity.TSchedule
import com.maple.herocalendarforbackend.entity.TScheduleMember
import com.maple.herocalendarforbackend.entity.TScheduleMemberGroup
import com.maple.herocalendarforbackend.entity.TScheduleNote
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TScheduleMemberGroupRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import com.maple.herocalendarforbackend.repository.TScheduleNoteRepository
import com.maple.herocalendarforbackend.repository.TScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Service
@Suppress("TooManyFunctions", "MagicNumber")
class ScheduleService(
    private val tScheduleRepository: TScheduleRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
    private val tUserRepository: TUserRepository,
    private val tScheduleMemberGroupRepository: TScheduleMemberGroupRepository,
    private val tScheduleNoteRepository: TScheduleNoteRepository,
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
            val tNote = if (request.note != null)
                tScheduleNoteRepository.save(TScheduleNote.convert(request.note))
            else null

            saveSchedule(request, requesterId, it, tNote)
            saveScheduleMember(owner, request.memberIds, it)
        }
    }

    @Transactional
    fun saveScheduleMember(owner: TUser, memberIds: List<String>, group: TScheduleMemberGroup) {
        val members =
            tUserRepository.findPublicOrFollowing(memberIds.filter { it != owner.id }.toSet().toList(), owner.id!!)
        val memberData = mutableListOf(
            TScheduleMember.initConvert(
                owner, group, ScheduleAcceptedStatus.ACCEPTED
            )
        )
        memberData.addAll(members.map {
            TScheduleMember.initConvert(
                it, group, ScheduleAcceptedStatus.WAITING
            )
        })

        tScheduleMemberRepository.saveAll(memberData)
    }

    @Transactional
    fun saveSchedule(
        request: ScheduleAddRequest,
        ownerId: String,
        memberGroup: TScheduleMemberGroup,
        noteGroup: TScheduleNote?
    ) {
        val requestStart = request.start
        val requestEnd =
            if (request.allDay == true) LocalDateTime.of(
                requestStart.year,
                requestStart.month,
                requestStart.dayOfMonth,
                23,
                59
            )
            else request.end ?: requestStart

        val diff = Duration.between(requestStart, requestEnd).toMinutes()
        val start = requestStart.toLocalDate()
        val end = when {
            (request.repeatInfo == null) -> requestEnd.toLocalDate()
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

        val parentSchedule = tScheduleRepository.save(TSchedule.convert(request, ownerId, memberGroup, noteGroup))
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

    @Transactional
    fun updateMember(requesterId: String, schedule: TSchedule, requestMemberIds: List<String>): TScheduleMemberGroup? {
        val exist = tScheduleMemberRepository.findByGroupKeyGroupId(schedule.memberGroup.id!!)
            .mapNotNull { it.groupKey.user.id }
        val invite = requestMemberIds.filter {
            !exist.contains(it)
        }
        return if (invite.isNotEmpty()) {
            tScheduleMemberGroupRepository.save(TScheduleMemberGroup()).let { group ->
                tScheduleMemberRepository.saveAll(
                    tUserRepository.findPublicOrFollowing(invite, requesterId)
                        .map {
                            TScheduleMember.initConvert(it, group, ScheduleAcceptedStatus.WAITING)
                        }
                )
                group
            }
        } else null
    }

    @Transactional
    fun update(requesterId: String, request: ScheduleUpdateRequest) {
        val schedule = findById(request.scheduleId)
        schedule.memberGroup.id?.let {
            tScheduleMemberRepository.findByGroupKeyGroupId(it).firstOrNull { m ->
                m.groupKey.user.id == requesterId
            } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        // note update
        val updateNote = if (request.note != null && schedule.note?.note != request.note) {
            tScheduleNoteRepository.save(TScheduleNote.convert(request.note))
        } else null

        val memberUpdate = updateMember(requesterId, schedule, request.memberIds)

        // 업데이트 사항이 있을 때만
        if (updateDataCompare(schedule, request) || updateNote != null) {
            val entities = when (request.scheduleUpdateCode) {
                ScheduleUpdateCode.ALL -> tScheduleRepository.findByParentId(schedule.parentId)
                ScheduleUpdateCode.ONLY_THIS -> listOf(schedule)
                ScheduleUpdateCode.THIS_AND_FUTURE -> tScheduleRepository.findByGroupAndAfterDay(
                    schedule.memberGroup.id,
                    schedule.start
                )
            }

            val startDiff = Duration.between(schedule.start, request.start).toMinutes()
            val requestEnd =
                if (request.allDay) LocalDateTime.of(
                    request.start.year,
                    request.start.month,
                    request.start.dayOfMonth,
                    23,
                    59
                )
                else request.end ?: request.start
            val endDiff = Duration.between(schedule.end, requestEnd).toMinutes()
            tScheduleRepository.saveAll(
                entities.map {
                    it.copy(
                        title = request.title,
                        start = it.start.plusMinutes(startDiff),
                        end = it.end.plusMinutes(endDiff),
                        allDay = request.allDay,
                        isPublic = request.isPublic,
                        note = updateNote ?: schedule.note,
                        memberGroup = memberUpdate ?: schedule.memberGroup
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
            ScheduleAcceptedStatus.ACCEPTED.toString()
        )
            ?.let {
                tScheduleMemberRepository.save(it.copy(acceptedStatus = ScheduleAcceptedStatus.ACCEPTED))
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
            ScheduleAcceptedStatus.REFUSED.toString()
        )?.let {
            tScheduleMemberRepository.save(it.copy(acceptedStatus = ScheduleAcceptedStatus.REFUSED))
        } ?: throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 로그인 유저의 스케줄 검색
     */
    fun findForPersonal(loginUserId: String, from: LocalDate, to: LocalDate): List<ScheduleResponse> {
        return convertToResponse(tScheduleRepository.findByFromToAndUserId(loginUserId, from, to))
    }

    fun findForPublic(
        loginUserId: String?,
        searchUserId: String,
        from: LocalDate,
        to: LocalDate
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
                        acceptedStatus = ScheduleAcceptedStatus.REFUSED
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
