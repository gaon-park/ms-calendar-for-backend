package com.maple.heroforbackend

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.request.ScheduleAddRequest
import com.maple.heroforbackend.entity.TSchedule
import com.maple.heroforbackend.entity.TScheduleMember
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.repository.TScheduleMemberRepository
import com.maple.heroforbackend.repository.TScheduleRepository
import com.maple.heroforbackend.repository.TUserRepository
import com.maple.heroforbackend.service.EmailSendService
import com.maple.heroforbackend.service.ScheduleService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime

class ScheduleServiceTest : BehaviorSpec() {
    init {
        val tScheduleRepository = mockk<TScheduleRepository>()
        val tScheduleMemberRepository = mockk<TScheduleMemberRepository>()
        val tUserRepository = mockk<TUserRepository>()
        val emailSendService = mockk<EmailSendService>()

        val service = ScheduleService(
            tScheduleRepository,
            tScheduleMemberRepository,
            tUserRepository,
            emailSendService
        )

        val tScheduleSlot: CapturingSlot<TSchedule> = slot()
        val tScheduleMemberSlot: CapturingSlot<List<TScheduleMember>> = slot()
        val tEmailSlot: CapturingSlot<List<String>> = slot()

        val owner = TUser(
            id = 1,
            email = "do.judo1224@gmail.com",
            nickName = "do.judo1224@gmail.com",
            pass = "",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val members = listOf(
            owner,
            TUser(
                id = 2,
                email = "helloproud25@naver.com",
                nickName = "helloproud25@naver.com",
                pass = "",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            TUser(
                id = 3,
                email = "helloproud68@naver.com",
                nickName = "helloproud68@naver.com",
                pass = "",
                verified = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        val request = ScheduleAddRequest(
            title = "first",
            start = LocalDateTime.now(),
            end = null,
            allDay = false,
            note = null,
            color = null,
            isPublic = null,
            members = emptyList()
        )

        afterContainer {
            tScheduleSlot.clear()
            tScheduleMemberSlot.clear()
            tEmailSlot.clear()
        }

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

        // method: insert
        Given("owner 를 제외한 멤버가 없는 상황에서") {
            every {
                tUserRepository.findByEmailIn(any())
            } answers {
                emptyList()
            }
            When("개인 스케줄 입력을 요청하면") {
                service.insert(owner, request)
                Then("1명의 스케줄 입력이 이루어진다") {
                    tScheduleSlot.captured.title shouldBe request.title
                    tScheduleMemberSlot.captured.size shouldBe 1
                    tScheduleMemberSlot.captured[0].user.id shouldBe owner.id
                }
            }

            When("파티 스케줄 입력을 요청하면") {
                val exception = shouldThrow<BaseException> {
                    service.insert(owner, request.copy(members = listOf(members[1].email)))
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
                tUserRepository.findByEmailIn(capture(tEmailSlot))
            } answers {
                members.filter { tEmailSlot.captured.contains(it.email) }
            }

            When("파티 스케줄을 입력하면(owner not in members)") {
                val curReq = request.copy(members = listOf(members[1].email, members[2].email))
                service.insert(owner, curReq)
                Then("스케줄과 request.members 데이터가 모두 입력된다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.title shouldBe curReq.title
                    tEmailSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.captured.size shouldBe curReq.members.size + 1
                }
            }

            When("파티 스케줄을 입력하면(owner in members)") {
                val curReq = request.copy(members = listOf(members[0].email, members[1].email, members[2].email))
                service.insert(owner, curReq)
                Then("스케줄과 request.members 데이터가 모두 입력된다") {
                    tScheduleSlot.isCaptured shouldBe true
                    tScheduleSlot.captured.title shouldBe curReq.title
                    tEmailSlot.isCaptured shouldBe true
                    tEmailSlot.captured.size shouldBe curReq.members.size - 1
                    tScheduleMemberSlot.isCaptured shouldBe true
                    tScheduleMemberSlot.captured.size shouldBe curReq.members.size
                }
            }
        }
    }
}
