package com.maple.herocalendarforbackend.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleOwnerChangeRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.service.ScheduleService
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
        val baseUri = "/user/schedule"

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

        describe("스케줄 입력") {
            val perform = { request: ScheduleAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post(baseUri)
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

        describe("스케줄 삭제") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.delete(baseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(ScheduleRequest(100)))
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

        describe("스케줄 멤버 추가") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleMemberAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            val req = ScheduleMemberAddRequest(100, listOf())
            context("요청자!=멤버") {
                every {
                    scheduleService.updateMember(
                        any(),
                        any(),
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
                every { scheduleService.updateMember(any(), any()) } just Runs
                val result = perform(req)
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("소유자 수정 요청") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleOwnerChangeRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("$baseUri/owner-change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody") {
                val result = perform(ScheduleOwnerChangeRequest(100, ""))
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
                val result = perform(ScheduleOwnerChangeRequest(100, "aa"))
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
                val result = perform(ScheduleOwnerChangeRequest(100, "aa"))
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("스케줄 소유자 변경 요청 수락") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/owner-change/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(ScheduleRequest(100)))
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

        describe("스케줄 소유자 변경 요청 거절") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/owner-change/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(ScheduleRequest(100)))
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

        describe("스케줄 수정(누구든 참석자인 경우 조정 가능)") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleUpdateRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put(baseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            val request = ScheduleUpdateRequest(
                scheduleId = 100,
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

        describe("스케줄 추가 요청 수락") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("없는 스케줄의 아이디") {
                every { scheduleService.scheduleAccept(any(), any()) } throws BaseException(BaseResponseCode.NOT_FOUND)
                val result = perform(ScheduleRequest(100))
                it("NOT_FOUND 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.NOT_FOUND.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.NOT_FOUND
                                )
                            )
                    }
                }
            }
            context("있는 스케줄, 멤버가 아닌 유저로부터의 요청") {
                every { scheduleService.scheduleAccept(any(), any()) } throws
                        BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(ScheduleRequest(100))
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
                every { scheduleService.scheduleAccept(any(), any()) } just Runs
                val result = perform(ScheduleRequest(100))
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("스케줄 추가 요청 거절") {
            every { accountService.findByEmail(any()) } returns user
            every { jwtAuthService.getUserName(any()) } returns ""
            val perform = { request: ScheduleRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("없는 스케줄의 아이디") {
                every { scheduleService.scheduleRefuse(any(), any()) } throws BaseException(BaseResponseCode.NOT_FOUND)
                val result = perform(ScheduleRequest(100))
                it("NOT_FOUND 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.NOT_FOUND.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.NOT_FOUND
                                )
                            )
                    }
                }
            }
            context("있는 스케줄, 멤버가 아닌 유저로부터의 요청") {
                every {
                    scheduleService.scheduleRefuse(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(ScheduleRequest(100))
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
                every { scheduleService.scheduleRefuse(any(), any()) } just Runs
                val result = perform(ScheduleRequest(100))
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }
    }
}
