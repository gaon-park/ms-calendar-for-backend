package com.maple.herocalendarforbackend.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.RepeatCode
import com.maple.herocalendarforbackend.config.JwtAuthenticationFilter
import com.maple.herocalendarforbackend.dto.request.RepeatInfo
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleOwnerChangeRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.service.ScheduleService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.MethodArgumentNotValidException
import java.nio.charset.StandardCharsets
import java.security.Principal
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("EmptyFunctionBlock")
class UserScheduleControllerTest : DescribeSpec() {
    init {
        val scheduleService = mockk<ScheduleService>()
        val jwtAuthService = mockk<JwtAuthService>(relaxed = true)
        val baseUri = "/user/schedule"

        val authentication = object : Authentication {
            override fun getName(): String = "name"
            override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
                mutableListOf(GrantedAuthority { "USER" })

            override fun getCredentials(): Any = "credentials"
            override fun getDetails(): Any = "details"
            override fun getPrincipal(): Any = "principal"
            override fun isAuthenticated(): Boolean = true
            override fun setAuthenticated(isAuthenticated: Boolean) {}
        }

        val principal = Principal { "username" }

        beforeContainer {
            every { jwtAuthService.getValidatedAuthData(any()) } returns "token-content"
            every { jwtAuthService.getAuthentication(any()) } returns authentication
        }

        afterContainer {
            clearAllMocks()
        }

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
            UserCalendarController(scheduleService)
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .addFilters<StandaloneMockMvcBuilder>(JwtAuthenticationFilter(jwtAuthService))
            .build()

        describe("스케줄 입력") {
            val perform = { request: ScheduleAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post(baseUri)
                        .servletPath(baseUri)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody_0") {
                val result = perform(
                    ScheduleAddRequest(
                        "",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1),
                        false,
                        null,
                        null,
                        null,
                        null,
                        emptyList()
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
            context("비정상 requestBody_1") {
                val result = perform(
                    ScheduleAddRequest(
                        "11",
                        LocalDateTime.now().plusMonths(1),
                        LocalDateTime.now(),
                        false,
                        null,
                        null,
                        null,
                        null,
                        emptyList()
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
            context("정상 요청") {
                every { scheduleService.save(any(), any()) } just Runs
                val result = perform(
                    ScheduleAddRequest(
                        "aa",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1),
                        true,
                        RepeatInfo(RepeatCode.DAYS, LocalDate.now(), LocalDate.now().plusMonths(1)),
                        null,
                        null,
                        null
                    )
                )
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }
//
//        describe("스케줄 삭제") {
//            val perform = {
//                mockMvc.perform(
//                    MockMvcRequestBuilders.delete("$baseUri/{scheduleId}", 1)
//                        .servletPath(baseUri)
//                        .principal(principal)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .characterEncoding(StandardCharsets.UTF_8)
//                        .accept(MediaType.APPLICATION_JSON)
//                )
//            }
//            context("common case: 존재하지 않은 스케줄") {
//                every { scheduleService.delete(any(), any()) } throws BaseException(BaseResponseCode.NOT_FOUND)
//                val result = perform()
//                it("NOT_FOUND 예외 발생") {
//                    result.andExpect {
//                        it.resolvedException?.javaClass shouldBeSameInstanceAs
//                                BaseException::class.java
//                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
//                            .writeValueAsString(
//                                ErrorResponse.convert(
//                                    BaseResponseCode.NOT_FOUND
//                                )
//                            )
//                    }
//                }
//            }
//            context("요청자!=소유자") {
//                every { scheduleService.delete(any(), any()) } throws BaseException(BaseResponseCode.BAD_REQUEST)
//                val result = perform()
//                it("BAD_REQUEST 예외 발생") {
//                    result.andExpect {
//                        it.resolvedException?.javaClass shouldBeSameInstanceAs
//                                BaseException::class.java
//                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
//                            .writeValueAsString(
//                                ErrorResponse.convert(
//                                    BaseResponseCode.BAD_REQUEST
//                                )
//                            )
//                    }
//                }
//            }
//            context("정상 요청") {
//                every { scheduleService.delete(any(), any()) } just Runs
//                val result = perform()
//                it("정상 종료") {
//                    result.andExpect {
//                        it.response.status shouldBe HttpStatus.OK.value()
//                    }
//                }
//            }
//        }

        describe("스케줄 수정(멤버 추가)") {
            val perform = { request: ScheduleMemberAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/members")
                        .servletPath("$baseUri/members")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("요청자!=멤버") {
                every { scheduleService.updateMember(any(), any()) } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(ScheduleMemberAddRequest(0, emptyList()))
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
            context("정상 요청") {
                every { scheduleService.updateMember(any(), any()) } just Runs
                val result = perform(ScheduleMemberAddRequest(0, emptyList()))
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("스케줄 소유자 위임 요청") {
            val perform = { request: ScheduleOwnerChangeRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/owner-change")
                        .servletPath("$baseUri/owner-change")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody") {
                val result = perform(ScheduleOwnerChangeRequest(0, ""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("등록(이메일 인증)되지 않은 유저 || 요청자 != 소유자") {
                every {
                    scheduleService.changeOwnerRequest(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(ScheduleOwnerChangeRequest(0, "do.judo1224@gmail.com"))
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
                every { scheduleService.changeOwnerRequest(any(), any()) } just Runs
                val result = perform(ScheduleOwnerChangeRequest(0, "do.judo1224@gmail.com"))
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("스케줄 소유자 변경 요청 수락") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/owner-change/accept")
                        .servletPath("$baseUri/owner-change/accept")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(ScheduleRequest(0)))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("스케줄!=요청 대기상태 && 요청자!=다음 소유자") {
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
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.put("$baseUri/owner-change/refuse")
                        .servletPath("$baseUri/owner-change/refuse")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(ScheduleRequest(0)))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("스케줄!=요청 대기상태 && 요청자!=다음 소유자") {
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

        describe("스케줄 수정") {
            val perform = { request: ScheduleUpdateRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.put(baseUri)
                        .servletPath(baseUri)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody_0") {
                val result = perform(
                    ScheduleUpdateRequest(
                        scheduleId = 0,
                        title = "",
                        start = LocalDateTime.now(),
                        end = null,
                        allDay = false,
                        note = null,
                        isPublic = false
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
            context("비정상 requestBody_1") {
                val result = perform(
                    ScheduleUpdateRequest(
                        scheduleId = 0,
                        title = "title",
                        start = LocalDateTime.now().plusMonths(1),
                        end = LocalDateTime.now(),
                        allDay = false,
                        note = null,
                        isPublic = false
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
            context("요청자!=멤버") {
                every {
                    scheduleService.update(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(
                    ScheduleUpdateRequest(
                        scheduleId = 0,
                        title = "title",
                        start = LocalDateTime.now(),
                        end = null,
                        allDay = false,
                        note = null,
                        isPublic = false
                    )
                )
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
                        any()
                    )
                } just Runs
                val result = perform(
                    ScheduleUpdateRequest(
                        scheduleId = 0,
                        title = "title",
                        start = LocalDateTime.now(),
                        end = null,
                        allDay = false,
                        note = null,
                        isPublic = false
                    )
                )
                it("정상 Response") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("로그인 유저의 스케줄") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get(baseUri)
                        .servletPath(baseUri)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", LocalDate.now().toString())
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("정상 요청") {
                every { scheduleService.findSchedulesAndConvertToResponse(any(), any(), any()) } returns emptyList()
                val result = perform()
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }
    }
}
