package com.maple.herocalendarforbackend.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.config.JwtAuthenticationFilter
import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.dto.request.FriendRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.FriendshipService
import com.maple.herocalendarforbackend.service.JwtAuthService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.MethodArgumentNotValidException
import java.nio.charset.StandardCharsets
import java.security.Principal

@Suppress("EmptyFunctionBlock")
class UserFriendControllerTest : DescribeSpec() {
    init {
        val friendshipService = mockk<FriendshipService>()
        val jwtAuthService = mockk<JwtAuthService>(relaxed = true)
        val baseUri = "/user/friend"

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
            UserFriendController(
                friendshipService
            )
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .addFilters<StandaloneMockMvcBuilder>(JwtAuthenticationFilter(jwtAuthService))
            .build()

        describe("친구 요청 보내기") {
            val perform = { request: FriendAddRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("$baseUri/add")
                        .servletPath("$baseUri/add")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody") {
                val result = perform(FriendAddRequest("", null))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("common case: 찾을 수 없는 유저") {
                every {
                    friendshipService.friendRequest(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.USER_NOT_FOUND)
                val result = perform(FriendAddRequest("1234", null))
                it("USER_NOT_FOUND 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.USER_NOT_FOUND
                                )
                            )
                    }
                }
            }
            context("요청자==응답자") {
                every {
                    friendshipService.friendRequest(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(FriendAddRequest("1234", null))
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
            context("이미 요청후, 응답 대기중/거절 당한 경우") {
                every {
                    friendshipService.friendRequest(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.WAITING_FOR_RESPONDENT)
                val result = perform(FriendAddRequest("1234", null))
                it("WAITING_FOR_RESPONDENT 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.WAITING_FOR_RESPONDENT
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every {
                    friendshipService.friendRequest(
                        any(),
                        any()
                    )
                } just Runs
                val result = perform(FriendAddRequest("1234", null))
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("친구 삭제") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.delete(baseUri)
                        .servletPath(baseUri)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(FriendRequest("aa")))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("이미 친구 아님") {
                every { friendshipService.deleteFriend(any(), any()) } throws
                        BaseException(BaseResponseCode.BAD_REQUEST)
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
            context("유저 없음") {
                every {
                    friendshipService.deleteFriend(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.USER_NOT_FOUND)
                val result = perform()
                it("USER_NOT_FOUND 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().registerModule(JavaTimeModule())
                            .writeValueAsString(
                                ErrorResponse.convert(
                                    BaseResponseCode.USER_NOT_FOUND
                                )
                            )
                    }
                }
            }
            context("정상 요청") {
                every { friendshipService.deleteFriend(any(), any()) } just Runs
                val result = perform()
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("친구 요청 수락") {
            val perform = { request: FriendRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/accept")
                        .servletPath("$baseUri/accept")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("common case: 비정상 requestBody") {
                val result = perform(FriendRequest(""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("요청 사실 확인 불가") {
                every {
                    friendshipService.friendRequestAccept(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(FriendRequest("1234"))
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
                every {
                    friendshipService.friendRequestAccept(
                        any(),
                        any()
                    )
                } just Runs
                val result = perform(FriendRequest("1234"))
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("친구 요청 거절") {
            val perform = { request: FriendRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/refuse")
                        .servletPath("$baseUri/refuse")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().registerModule(JavaTimeModule()).writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("common case: 비정상 requestBody") {
                val result = perform(FriendRequest(""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("요청 사실 확인 불가") {
                every {
                    friendshipService.friendRequestRefuse(
                        any(),
                        any()
                    )
                } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val result = perform(FriendRequest("1234"))
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
                every {
                    friendshipService.friendRequestRefuse(
                        any(),
                        any()
                    )
                } just Runs
                val result = perform(FriendRequest("1234"))
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("로그인 유저의 친구 리스트") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get(baseUri)
                        .servletPath(baseUri)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("정상 요청") {
                every { friendshipService.findFriends(any()) } returns emptyList()
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
