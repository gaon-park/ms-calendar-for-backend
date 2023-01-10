package com.maple.herocalendarforbackend.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.FriendshipService
import com.maple.herocalendarforbackend.service.JwtAuthService
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
                accountService, friendshipService
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
                        personalKey = "",
                        null
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
                val result = perform(
                    FriendAddRequest(
                        personalKey = "aa",
                        null
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
                every { friendshipService.friendRequest(any(), any()) } just Runs
                val result = perform(
                    FriendAddRequest(
                        personalKey = "aa",
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
