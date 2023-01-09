package com.maple.herocalendarforbackend.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.AccountRegistRequest
import com.maple.herocalendarforbackend.dto.request.LoginRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.LoginResponse
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.EmailTokenService
import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.service.LoginService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.MethodArgumentNotValidException
import java.nio.charset.StandardCharsets

class AccountControllerTest : DescribeSpec() {
    init {
        val accountService = mockk<AccountService>()
        val loginService = mockk<LoginService>()
        val emailTokenService = mockk<EmailTokenService>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val jwtAuthService = mockk<JwtAuthService>(relaxed = true)

        val userSaveSlot: CapturingSlot<AccountRegistRequest> = slot()
        val emailSlot: CapturingSlot<String> = slot()

        afterContainer {
            userSaveSlot.clear()
            emailSlot.clear()
        }

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
            AccountController(
                accountService, loginService, emailTokenService, passwordEncoder, jwtAuthService
            )
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .build()

        every { passwordEncoder.encode(any()) } answers { this.value }
        every { passwordEncoder.matches(any(), any()) } answers {
            (this.args[0] == this.args[1])
        }

        val addRequest = AccountRegistRequest(
            email = "do.judo1224@gmail.com",
            password = "qwer1234",
            confirmPassword = "qwer1234",
            nickName = null,
            isPublic = null
        )
        val user = TUser.generateSaveModel(addRequest, passwordEncoder).copy(id = "aa")

        describe("post: /login") {
            val perform = { request: LoginRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 LoginRequest") {
                val result = perform(LoginRequest(email = "", password = ""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("정상 LoginRequest 이지만, 해당 유저가 없는 상황") {
                every { loginService.loadUserByUsername(any()) } returns null
                val result = perform(LoginRequest(email = user.email, password = user.password))
                it("USER_NOT_FOUND 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.USER_NOT_FOUND.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().writeValueAsString(
                            ErrorResponse.convert(
                                BaseResponseCode.USER_NOT_FOUND
                            )
                        )
                    }
                }
            }
            context("정상 LoginRequest 이고 해당 유저가 있지만, 패스워드 불일치") {
                every { loginService.loadUserByUsername(any()) } returns user
                val result = perform(LoginRequest(user.email, "12341234"))
                it("INVALID_PASSWORD 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.status shouldBe BaseResponseCode.INVALID_PASSWORD.httpStatus.value()
                        it.response.contentAsString shouldBe jsonMapper().writeValueAsString(
                            ErrorResponse.convert(
                                BaseResponseCode.INVALID_PASSWORD
                            )
                        )
                    }
                }
            }
            context("정상 LoginRequest 이고 해당 유저가 존재하며 패스워드 일치") {
                every { loginService.loadUserByUsername(any()) } returns user
                val result = perform(LoginRequest(user.email, user.password))
                it("JWT_TOKEN 발행") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                        val res =
                            jacksonObjectMapper().readValue(it.response.contentAsString, LoginResponse::class.java)
                        res shouldNotBe null
                        res!!.accessToken shouldNotBe null
                    }
                }
            }
        }

        describe("post: /account/regist") {
            every { emailTokenService.sendEmailToken(any(), any()) } returns ""
            val perform = { request: AccountRegistRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("/account/regist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 AccountRegistRequest_0") {
                val result = perform(
                    AccountRegistRequest(
                        email = "",
                        password = "",
                        confirmPassword = "",
                        nickName = null,
                        isPublic = null
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
            context("비정상 AccountRegistRequest_1") {
                val result = perform(
                    AccountRegistRequest(
                        email = "do.judo1224@gmail.com",
                        password = "qwer",
                        confirmPassword = "1234",
                        nickName = null,
                        isPublic = null
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
            context("정상 AccountRegistRequest 이지만, 등록정보 있음") {
                every { accountService.save(any()) } throws BaseException(BaseResponseCode.DUPLICATE_EMAIL)
                val result = perform(addRequest)
                it("DUPLICATE_EMAIL 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().writeValueAsString(
                            ErrorResponse.convert(
                                BaseResponseCode.DUPLICATE_EMAIL
                            )
                        )
                    }
                }
            }
            context("정상 AccountRegistRequest 이고 등록정보 없음") {
                every { accountService.save(capture(userSaveSlot)) } returns user
                every { emailTokenService.sendEmailToken(any(), capture(emailSlot)) } returns ""
                val result = perform(addRequest)
                it("데이터 저장 후 인증 메일 발송") {
                    emailSlot.isCaptured shouldBe true
                    emailSlot.captured shouldBe user.email
                    userSaveSlot.isCaptured shouldBe true
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("get: /confirm-email") {
            val perform = { token: String ->
                mockMvc.perform(
                    MockMvcRequestBuilders.get("/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("token", token)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 token") {
                every { emailTokenService.verifyEmail(any()) } throws BaseException(BaseResponseCode.INVALID_AUTH_TOKEN)
                val result = perform("")
                it("INVALID_AUTH_TOKEN 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().writeValueAsString(
                            ErrorResponse.convert(
                                BaseResponseCode.INVALID_AUTH_TOKEN
                            )
                        )
                    }
                }
            }
            context("정상 token") {
                every { emailTokenService.verifyEmail(any()) } returns user.copy(verified = true)
                every { loginService.loadUserByUsername(any()) } returns user
                val result = perform("")
                it("이메일 인증 후 로그인 상태로") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                        val res =
                            jacksonObjectMapper().readValue(it.response.contentAsString, LoginResponse::class.java)
                        res shouldNotBe null
                        res.accessToken shouldNotBe null
                    }
                }
            }
        }
    }
}
