package com.maple.heroforbackend.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.heroforbackend.api.base.ExceptionHandler
import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.request.FriendAddRequest
import com.maple.heroforbackend.dto.response.ErrorResponse
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.FriendshipService
import com.maple.heroforbackend.service.JwtAuthService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.MethodArgumentNotValidException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class UserFriendControllerTest : DescribeSpec() {
    init {
        val jwtAuthService = mockk<JwtAuthService>()
        val accountService = mockk<AccountService>()
        val friendshipService = mockk<FriendshipService>()

        val baseUri = "/user/friend"
        val user = TUser(
            id = "0",
            email = "do.judo1224@gmail.com",
            nickName = "",
            pass = "",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isPublic = false
        )

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
            UserFriendController(
                jwtAuthService, accountService, friendshipService
            )
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .build()

        describe("post: /add") {
            val perform = { request: FriendAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("$baseUri/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 데이터") {
                val result = perform(
                    FriendAddRequest(
                        personalKey = ""
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
            context("정상 데이터, 로그아웃/유효하지 않은 토큰") {
                every { accountService.findByEmail(any()) } returns null
                every { jwtAuthService.getUserName(any()) } returns ""
                val result = perform(
                    FriendAddRequest(
                        personalKey = "aa"
                    )
                )
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
                every { accountService.findByEmail(any()) } returns user
                every { jwtAuthService.getUserName(any()) } returns ""
                every { friendshipService.friendRequest(any(), any()) } just Runs
                val result = perform(
                    FriendAddRequest(
                        personalKey = "aa"
                    )
                )
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("get: /accept") {
            val perform = { from: String ->
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", from)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("로그아웃/유효하지 않은 토큰") {
                every { accountService.findByEmail(any()) } returns null
                every { jwtAuthService.getUserName(any()) } returns ""
                val result = perform("")
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
                every { accountService.findByEmail(any()) } returns user
                every { jwtAuthService.getUserName(any()) } returns ""
                every { friendshipService.friendRequestAccept(any(), any()) } just Runs
                val result = perform("aa")
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("get: /refuse") {
            val perform = { from: String ->
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("from", from)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("로그아웃/유효하지 않은 토큰") {
                every { accountService.findByEmail(any()) } returns null
                every { jwtAuthService.getUserName(any()) } returns ""
                val result = perform("")
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
                every { accountService.findByEmail(any()) } returns user
                every { jwtAuthService.getUserName(any()) } returns ""
                every { friendshipService.friendRequestRefuse(any(), any()) } just Runs
                val result = perform("aa")
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }
    }
}
