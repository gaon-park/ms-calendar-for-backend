package com.maple.herocalendarforbackend.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.config.JwtAuthenticationFilter
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
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.MethodArgumentNotValidException
import java.nio.charset.StandardCharsets

@Suppress("EmptyFunctionBlock")
class AccountControllerTest : DescribeSpec() {
    init {
        val accountService = mockk<AccountService>()
        val loginService = mockk<LoginService>()
        val emailTokenService = mockk<EmailTokenService>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val jwtAuthService = mockk<JwtAuthService>(relaxed = true)

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

        beforeContainer {
            every { jwtAuthService.firstTokenForLogin(any(), any(), any()) } returns "token-content"
            every { jwtAuthService.getValidatedAuthData(any()) } returns "token-content"
            every { jwtAuthService.getAuthentication(any()) } returns authentication
        }

        afterContainer {
            clearAllMocks()
        }

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
            AccountController(
                accountService, loginService, emailTokenService, passwordEncoder, jwtAuthService
            )
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .addFilters<StandaloneMockMvcBuilder>(JwtAuthenticationFilter(jwtAuthService))
            .build()

        describe("최초 로그인, JWT TOKEN 발행") {
            val perform = { request: LoginRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("/login")
                        // filter 제외 url 이 되기 때문
//                        .servletPath("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody") {
                val result = perform(LoginRequest(email = "", password = ""))
                it("BAD_REQUEST 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs
                                MethodArgumentNotValidException::class.java
                        it.response.status shouldBe HttpStatus.BAD_REQUEST.value()
                    }
                }
            }
            context("해당 유저 없음") {
                every { loginService.loadUserByUsername(any()) } returns null
                val result = perform(LoginRequest("do.judo1224@gmail.com", "11"))
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
            context("패스워드 불일치") {
                every { loginService.loadUserByUsername(any()) } returns TUser.generateTmpModel()
                every { passwordEncoder.matches(any(), any()) } returns false
                val result = perform(LoginRequest("do.judo1224@gmail.com", "11"))
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
            context("정상 요청") {
                every { loginService.loadUserByUsername(any()) } returns TUser.generateTmpModel()
                every { passwordEncoder.matches(any(), any()) } returns true

                val result = perform(LoginRequest("do.judo1224@gmail.com", "11"))
                it("ACCESS_TOKEN 발행") {
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

        describe("refresh token 으로 access token 재발급") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("/reissue/access-token")
                        .servletPath("/reissue/access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("filter 처리 정상") {
                val result = perform()
                it("ACCESS_TOKEN 발행") {
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

        describe("회원 가입") {
            every { emailTokenService.sendEmailToken(any(), any()) } returns ""
            val perform = { request: AccountRegistRequest ->
                mockMvc.perform(
                    MockMvcRequestBuilders.post("/account/regist")
                        .servletPath("/account/regist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(jsonMapper().writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 requestBody_0") {
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
            context("이미 등록된 이메일") {
                every { accountService.save(any()) } throws BaseException(BaseResponseCode.DUPLICATE_EMAIL)
                val result = perform(
                    AccountRegistRequest(
                        email = "do.judo1224@gmail.com",
                        password = "1234",
                        confirmPassword = "1234",
                        nickName = null,
                        isPublic = null
                    )
                )
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
            context("정상 요청") {
                every { accountService.save(any()) } returns TUser.generateTmpModel()
                every { emailTokenService.sendEmailToken(any(), any()) } returns ""
                val result = perform(
                    AccountRegistRequest(
                        email = "do.judo1224@gmail.com",
                        password = "1234",
                        confirmPassword = "1234",
                        nickName = null,
                        isPublic = null
                    )
                )
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }

        describe("이메일 인증 완료 설정") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("/confirm-email")
                        .servletPath("/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("token", "token")
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("비정상 토큰") {
                every { emailTokenService.verifyEmail(any()) } throws BaseException(BaseResponseCode.INVALID_TOKEN)
                val result = perform()
                it("INVALID_TOKEN 예외 발생") {
                    result.andExpect {
                        it.resolvedException?.javaClass shouldBeSameInstanceAs BaseException::class.java
                        it.response.contentAsString shouldBe jsonMapper().writeValueAsString(
                            ErrorResponse.convert(
                                BaseResponseCode.INVALID_TOKEN
                            )
                        )
                    }
                }
            }
            context("정상 토큰") {
                every { emailTokenService.verifyEmail(any()) } just Runs
                every { loginService.loadUserByUsername(any()) } returns TUser.generateTmpModel()
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
