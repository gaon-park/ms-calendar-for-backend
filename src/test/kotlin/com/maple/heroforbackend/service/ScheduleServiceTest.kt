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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import java.util.*

class ScheduleServiceTest : BehaviorSpec() {
    init {
        val tScheduleRepository = mockk<TScheduleRepository>()
        val tScheduleMemberRepository = mockk<TScheduleMemberRepository>()
        val tUserRepository = mockk<TUserRepository>()
        val emailSendService = mockk<EmailSendService>()

        val service = ScheduleService(
            tScheduleRepository, tScheduleMemberRepository, tUserRepository, emailSendService
        )

        val tScheduleSlot: CapturingSlot<TSchedule> = slot()
        val tScheduleMemberSlot: CapturingSlot<List<TScheduleMember>> = slot()
        val emailSlot: CapturingSlot<List<String>> = slot()
        val idSlot: CapturingSlot<Long> = slot()
        val idSlotForDeleteSchedule: CapturingSlot<Long> = slot()
        val idSlotForDeleteByScheduleId: CapturingSlot<Long> = slot()
        val idSlotForDeleteScheduleIdAndUserId: CapturingSlot<Long> = slot()
        val slotForSendEmail: CapturingSlot<String> = slot()

        afterContainer {
            tScheduleSlot.clear()
            tScheduleMemberSlot.clear()
            emailSlot.clear()
            idSlot.clear()
            idSlotForDeleteSchedule.clear()
            idSlotForDeleteByScheduleId.clear()
            idSlotForDeleteScheduleIdAndUserId.clear()
            slotForSendEmail.clear()
        }

        val owner = TUser(
            id = 0,
            email = "do.judo1224@gmail.com",
            nickName = "do.judo1224@gmail.com",
            pass = "",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val members = listOf(
            owner, TUser(
                id = 1,
                email = "helloproud25@naver.com",
                nickName = "helloproud25@naver.com",
                pass = "",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ), TUser(
                id = 2,
                email = "helloproud68@naver.com",
                nickName = "helloproud68@naver.com",
                pass = "",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        val addRequest = ScheduleAddRequest(
            title = "first",
            start = LocalDateTime.now(),
            end = null,
            allDay = false,
            note = null,
            color = null,
            isPublic = null,
            members = emptyList()
        )
        var schedule = TSchedule.convert(addRequest, owner.id).copy(id = 0)
        schedule = schedule.copy(
            members = listOf(
                TScheduleMember(
                    id = null, schedule = schedule, user = members[0], accepted = true
                ), TScheduleMember(
                    id = null, schedule = schedule, user = members[1], accepted = false
                )
            )
        )
        val schedules = listOf(schedule)

        every {
            tScheduleRepository.save(capture(tScheduleSlot))
        } answers {
            tScheduleSlot.captured
        }

        every {
            tScheduleMemberRepository.saveAll(capture(tScheduleMemberSlot))
        } answers {
            tScheduleMemberSlot.captured
        }

        every {
            tScheduleRepository.deleteById(capture(idSlotForDeleteSchedule))
        } just Runs

        every {
            tScheduleMemberRepository.deleteByScheduleId(capture(idSlotForDeleteByScheduleId))
        } just Runs

        every {
            tScheduleMemberRepository.deleteByScheduleIdAndUserId(
                capture(idSlotForDeleteByScheduleId), capture(idSlotForDeleteScheduleIdAndUserId)
            )
        } just Runs

        // method: insert
        Given("owner 를 제외한 멤버가 없는 상황에서") {
            every {
                tUserRepository.findByEmailIn(any())
            } answers {
                emptyList()
            }
            When("개인 스케줄 입력을 요청하면") {
                service.insert(owner, addRequest)
                Then("1명의 스케줄 입력이 이루어진다") {
                    tScheduleSlot.captured.title shouldBe addRequest.title
                    tScheduleMemberSlot.captured.size shouldBe 1
                    tScheduleMemberSlot.captured[0].user.id shouldBe owner.id
                }
            }

            When("파티 스케줄 입력을 요청하면") {
                val exception = shouldThrow<BaseException> {
                    service.insert(owner, addRequest.copy(members = listOf(members[1].email)))
                }
                Then("USER_NOT_FOUND 예외가 발생한다") {
                    tScheduleMemberSlot.isCaptured shouldBe false
                    exception.errorCode shouldBe BaseResponseCode.USER_NOT_FOUND
                }
            }
        }

        // method: insert
        Given("owner 를 제외한 멤버가 있는 상황에서") {
            every {
                tUserRepository.findByEmailIn(capture(emailSlot))
            } answers {
                members.filter { emailSlot.captured.contains(it.email) }
            }

            When("파티 스케줄을 입력하면(owner not in members)") {
                val curReq = addRequest.copy(members = listOf(members[1].email, members[2].email))
                service.insert(owner, curReq)
                Then("스케줄과 request.members 데이터가 모두 입력된다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.title shouldBe curReq.title
                    emailSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.captured.size shouldBe curReq.members.size + 1
                }
            }

            When("파티 스케줄을 입력하면(owner in members)") {
                val curReq = addRequest.copy(members = listOf(members[0].email, members[1].email, members[2].email))
                service.insert(owner, curReq)
                Then("스케줄과 request.members 데이터가 모두 입력된다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.title shouldBe curReq.title
                    emailSlot.isCaptured shouldBe true
                    emailSlot.captured.size shouldBe curReq.members.size - 1
                    tScheduleMemberSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.captured.size shouldBe curReq.members.size
                }
            }
        }

        // method: selectById
        Given("스케줄 검색을 요청받은 상황에서") {
            When("scheduleId 가 존재하지 않는 스케줄의 id 이면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.selectById(1)
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                }
            }
            When("scheduleId 가 존재하는 스케줄의 id 이면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(schedule) }
                val res = service.selectById(schedule.id!!)
                Then("해당 스케줄 객체를 반환한다") {
                    res.id shouldBe schedules[0].id
                }
            }
        }

        // method: delete
        Given("스케줄 삭제를 요청받은 상황에서") {
            When("스케줄이 존재하지 않으면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.delete(1, owner)
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                }
            }

            every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(schedule) }
            When("스케줄이 존재하지만 (요청자!=소유자 && 요청자==멤버)이면") {
                service.delete(schedule.id!!, members[1])
                Then("요청자를 해당 스케줄 멤버에서 삭제한다") {
                    idSlotForDeleteByScheduleId.captured shouldBe 0
                    idSlotForDeleteScheduleIdAndUserId.captured shouldBe members[1].id
                }
            }
            When("스케줄이 존재하지만 (요청자!=소유자 && 요청자!=멤버)이면") {
                val exception = shouldThrow<BaseException> {
                    service.delete(0, members[2])
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    idSlotForDeleteSchedule.isCaptured shouldBe false
                    idSlotForDeleteByScheduleId.isCaptured shouldBe false
                    idSlotForDeleteScheduleIdAndUserId.isCaptured shouldBe false
                }
            }
        }

        // method: updateMember
        Given("멤버 추가를 요청받은 상황에서") {
            When("스케줄이 존재하지 않으면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.updateMember(100, owner, ScheduleMemberAddRequest())
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                    tScheduleMemberSlot.isCaptured shouldBe false
                }
            }

            every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(schedule) }
            When("스케줄이 존재하지만 (요청자!=멤버)이면") {
                val exception = shouldThrow<BaseException> {
                    service.updateMember(schedule.id!!, members[2], ScheduleMemberAddRequest())
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleMemberSlot.isCaptured shouldBe false
                }
            }
            When("요청자가 멤버이고 해당 스케줄이 존재하지만, 추가멤버가 기존멤버로만 이루어져있으면") {
                service.updateMember(schedule.id!!, owner, ScheduleMemberAddRequest(listOf(owner.email)))
                Then("DB 등록이 일어나지 않는다") {
                    tScheduleMemberSlot.isCaptured shouldBe false
                }
            }
            When("요청자가 멤버이고 해당 스케줄이 존재하지만, 추가멤버가 기존멤버+신규멤버(DB 등록o)로 이루어져있으면") {
                every {
                    tUserRepository.findByEmailIn(any())
                } answers {
                    listOf(members[2])
                }

                service.updateMember(schedule.id!!, owner, ScheduleMemberAddRequest(members.map { it.email }))
                Then("DB에 신규멤버만이 등록된다") {
                    tScheduleMemberSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.captured.size shouldBe 1
                    tScheduleMemberSlot.captured[0].user.email shouldBe members[2].email
                }
            }
            When("요청자가 멤버이고 해당 스케줄이 존재하지만, 추가멤버가 기존멤버+신규멤버(DB 등록x)로 이루어져있으면") {
                every {
                    tUserRepository.findByEmailIn(any())
                } answers {
                    emptyList()
                }

                service.updateMember(schedule.id!!, owner, ScheduleMemberAddRequest(members.map { it.email }))
                Then("DB 등록이 일어나지 않는다") {
                    tScheduleMemberSlot.isCaptured shouldBe false
                }
            }
        }

        // method: changeOwnerRequest
        Given("스케줄 소유자 위임 요청을 받은 상황에서") {
            every { emailSendService.sendOwnerChangeRequestEmail(any(), capture(slotForSendEmail)) } just Runs
            When("스케줄이 존재하지 않으면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerRequest(100, members[0], "")
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                    tScheduleMemberSlot.isCaptured shouldBe false
                }
            }

            every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(schedule) }
            When("스케줄이 존재하지만, 요청자!=소유자이면") {
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerRequest(schedule.id!!, members[1], "")
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleMemberSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==소유자이지만, 다음 소유자가 등록되지 않았거나 미인증 상태의 사용자이면") {
                every { tUserRepository.findByEmailAndVerified(any(), any()) } answers { null }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerRequest(schedule.id!!, owner, "")
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    slotForSendEmail.isCaptured shouldBe false
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==소유자이지만, 다음 소유자가 인증 완료 상태의 사용자이면") {
                every { tUserRepository.findByEmailAndVerified(any(), any()) } answers { members[2] }
                service.changeOwnerRequest(schedule.id!!, owner, members[2].email)
                Then("다음 소유자에게 메일이 발송되고 해당 스케줄이 소유자 변경 요청 상태로 바뀐다") {
                    slotForSendEmail.isCaptured shouldBe true
                    slotForSendEmail.captured shouldBe members[2].email
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.nextOwnerId shouldNotBe null
                    tScheduleSlot.captured.nextOwnerId shouldBe members[2].id
                    tScheduleSlot.captured.waitingOwnerChange shouldBe true
                }
            }
        }

        // method: changeOwnerAccept
        Given("스케줄 소유자 위임 승인 요청을 받은 상황에서") {
            When("스케줄이 존재하지 않으면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerAccept(100, members[0])
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                    tScheduleSlot.isCaptured shouldBe false
                }
            }

            When("스케줄이 존재하지만, 요청자!=다음 소유자이면") {
                every {
                    tScheduleRepository.findById(capture(idSlot))
                } answers {
                    Optional.of(schedule.copy(nextOwnerId = members[1].id))
                }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerAccept(schedules[0].id!!, members[2])
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==다음 소유자이지만, 스케줄이 위임 승인 대기중이 아니라면") {
                every {
                    tScheduleRepository.findById(capture(idSlot))
                } answers {
                    Optional.of(schedule.copy(nextOwnerId = members[1].id, waitingOwnerChange = false))
                }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerAccept(schedules[0].id!!, members[1])
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==다음 소유자이며 스케줄이 위임 승인 대기중이라면") {
                every {
                    tScheduleRepository.findById(capture(idSlot))
                } answers {
                    Optional.of(schedule.copy(nextOwnerId = members[1].id, waitingOwnerChange = true))
                }
                service.changeOwnerAccept(schedules[0].id!!, members[1])
                Then("스케줄 소유자를 변경하고 소유자 위임 상태를 해제한다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.nextOwnerId shouldBe null
                    tScheduleSlot.captured.waitingOwnerChange shouldBe false
                    tScheduleSlot.captured.ownerId shouldBe members[1].id
                }
            }
        }

        // method: changeOwnerRefuse
        Given("스케줄 소유자 변경 거절 요청을 받은 상황에서") {
            When("스케줄이 존재하지 않으면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerRefuse(100, members[0])
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하지만, 요청자!=다음 소유자이면") {
                every {
                    tScheduleRepository.findById(capture(idSlot))
                } answers {
                    Optional.of(schedule.copy(nextOwnerId = members[1].id))
                }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerRefuse(schedules[0].id!!, members[2])
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==다음 소유자이지만, 스케줄이 위임 승인 대기중이 아니라면") {
                every {
                    tScheduleRepository.findById(capture(idSlot))
                } answers {
                    Optional.of(schedule.copy(nextOwnerId = members[1].id, waitingOwnerChange = false))
                }
                val exception = shouldThrow<BaseException> {
                    service.changeOwnerRefuse(schedules[0].id!!, members[1])
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==다음 소유자이며 스케줄이 위임 승인 대기중이라면") {
                every {
                    tScheduleRepository.findById(capture(idSlot))
                } answers {
                    Optional.of(schedule.copy(nextOwnerId = members[1].id, waitingOwnerChange = true))
                }
                service.changeOwnerRefuse(schedules[0].id!!, members[1])
                Then("스케줄 소유자를 변경하지 않고 소유자 위임 상태를 해제한다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.nextOwnerId shouldBe null
                    tScheduleSlot.captured.waitingOwnerChange shouldBe false
                    tScheduleSlot.captured.ownerId shouldBe members[0].id
                }
            }
        }

        // method: update
        Given("스케줄 갱신 요청을 받은 상황에서") {
            val updateRequest = ScheduleUpdateRequest(
                title = "updated",
                start = LocalDateTime.now(),
                end = null,
                allDay = false,
                note = null,
                color = null,
                isPublic = true
            )
            When("스케줄이 존재하지 않으면") {
                every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(null) }
                val exception = shouldThrow<BaseException> {
                    service.update(100, owner, updateRequest)
                }
                Then("NOT_FOUND 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.NOT_FOUND
                    tScheduleSlot.isCaptured shouldBe false
                }
            }

            every { tScheduleRepository.findById(any()) } answers { Optional.ofNullable(schedule) }
            When("스케줄이 존재하지만, 요청자!=멤버라면") {
                val exception = shouldThrow<BaseException> {
                    service.update(0, members[2], updateRequest)
                }
                Then("BAD_REQUEST 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                    tScheduleSlot.isCaptured shouldBe false
                }
            }
            When("스케줄이 존재하고 요청자==멤버라면") {
                service.update(0, members[1], updateRequest)
                Then("스케줄 내용이 변경된다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.title shouldBe "updated"
                    tScheduleSlot.captured.isPublic shouldBe true
                }
            }
        }
    }
}
