package com.maple.heroforbackend.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.heroforbackend.api.base.ExceptionHandler
import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.request.ScheduleAddRequest
import com.maple.heroforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.heroforbackend.dto.request.ScheduleOwnerChangeRequest
import com.maple.heroforbackend.dto.request.ScheduleUpdateRequest
import com.maple.heroforbackend.dto.response.ErrorResponse
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.JwtAuthService
import com.maple.heroforbackend.service.ScheduleService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.MethodArgumentNotValidException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class UserScheduleControllerTest : DescribeSpec() {
    init {
        val jwtAuthService = mockk<JwtAuthService>()
        val accountService = mockk<AccountService>()
        val scheduleService = mockk<ScheduleService>()

        val tScheduleSlot: CapturingSlot<ScheduleAddRequest> = slot()
        val baseUri = "/user/calendar"

        val user = TUser(
            id = "0",
            email = "do.judo1224@gmail.com",
            nickName = "do.judo1224@gmail.com",
            pass = "",
            verified = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isPublic = false,
        )

        afterContainer {
            tScheduleSlot.clear()
        }

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
            UserCalendarController(
                jwtAuthService, accountService, scheduleService
            )
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .build()

        every { scheduleService.save(any(), capture(tScheduleSlot)) } just Runs

        describe("post: /schedule") {
            val perform = { request: ScheduleAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("$baseUri/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 ScheduleAddRequest") {
                val result = perform(
                    ScheduleAddRequest(
                        "",
                        LocalDateTime.now(),
                        null,
                        null,
                        null,
                        null,
                    )
                )
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("정상 ScheduleAddRequest 이지만 헤더에 토큰 정보가 없거나 등록되지 않은 유저(common context)") {
                every { accountService.findByEmail(any()) } returns null
                every { jwtAuthService.getUserName(any()) } returns ""
                val result = perform(
                    ScheduleAddRequest(
                        "title",
                        LocalDateTime.now(),
                        null,
                        null,
                        null,
                        null,
                    )
                )
                it("BAD_REQUEST 예외 발생") {
                    tScheduleSlot.isCaptured shouldBe false
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every { accountService.findByEmail(any()) } returns user
                every { jwtAuthService.getUserName(any()) } returns ""
                val result = perform(
                    ScheduleAddRequest(
                        "title",
                        LocalDateTime.now(),
                        null,
                        null,
                        null,
                        null,
                    )
                )
                it("db save") {
                    tScheduleSlot.isCaptured shouldBe true
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("delete: /schedule/{scheduleId}") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.delete("$baseUri/schedule/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("존재하지 않은 스케줄(common context)") {
                every { scheduleService.delete(any(), any()) } throws BaseException(BaseResponseCode.NOT_FOUND)
                val result = perform()
                it("NOT_FOUND 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.NOT_FOUND
                                )
                            )
                    }
                }
            }
            context("존재하는 스케줄이지만, 요청자!=소유자") {
                every { scheduleService.delete(any(), any()) } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform()
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("존재하는 스케줄이고 요청자==소유자") {
                every { scheduleService.delete(any(), any()) } just Runs
                val result = perform()
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("put: /schedule/members/{scheduleId} - 스케줄 멤버 추가") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleMemberAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/schedule/members/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            val req = ScheduleMemberAddRequest(listOf())
            context("요청자!=멤버") {
                every {
                    scheduleService.updateMember(
                        any(),
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(req)
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("요청자==멤버") {
                every { scheduleService.updateMember(any(), any(), any()) } just Runs
                val result = perform(req)
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("put: /schedule/{scheduleId}/owner-change") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleOwnerChangeRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/schedule/0/owner-change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody") {
                val result = perform(ScheduleOwnerChangeRequest(""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("정상 requestBody, 등록되지 않은 유저이거나 요청자!=소유자") {
                every {
                    scheduleService.changeOwnerRequest(
                        any(),
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(ScheduleOwnerChangeRequest("aa"))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.BAD_REQUEST.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every {
                    scheduleService.changeOwnerRequest(
                        any(),
                        any(),
                        any()
                    )
                } just Runs
                val result = perform(ScheduleOwnerChangeRequest("aa"))
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("get: /schedule/{scheduleId}/owner-change/accept") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/schedule/0/owner-change/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("스케줄이 요청대기상태가 아니거나 요청자!=다음 소유자") {
                every {
                    scheduleService.changeOwnerAccept(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform()
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.BAD_REQUEST.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every { scheduleService.changeOwnerAccept(any(), any()) } just Runs
                val result = perform()
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("get: /schedule/{scheduleId}/owner-change/refuse") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/schedule/0/owner-change/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("스케줄이 요청대기상태가 아니거나 요청자!=다음 소유자") {
                every {
                    scheduleService.changeOwnerRefuse(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform()
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.BAD_REQUEST.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every { scheduleService.changeOwnerRefuse(any(), any()) } just Runs
                val result = perform()
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("put: /schedule/{scheduleId} - 스케줄 수정") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleUpdateRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/schedule/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            val request = ScheduleUpdateRequest(
                title = "title",
                start = LocalDateTime.now(),
                end = null,
                allDay = false,
                note = null,
                isPublic = false
            )
            context("비정상 requestBody") {
                val result = perform(request.copy(title = ""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("정상 requestBody, 요청자!=멤버") {
                every {
                    scheduleService.update(
                        any(),
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(request)
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.BAD_REQUEST.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.BAD_REQUEST
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every {
                    scheduleService.update(
                        any(),
                        any(),
                        any()
                    )
                } just Runs
                val result = perform(request)
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }
    }
}
