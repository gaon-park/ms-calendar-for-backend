package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.entity.TEmailToken
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TEmailTokenRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import java.util.*

class EmailTokenServiceTest : BehaviorSpec() {
    init {
        val emailSendService = mockk<EmailSendService>()
        val tEmailTokenRepository = mockk<TEmailTokenRepository>()
        val tUserRepository = mockk<TUserRepository>()

        val service = EmailTokenService(
            emailSendService, tEmailTokenRepository, tUserRepository
        )

        val tUserSlot: CapturingSlot<TUser> = slot()
        val tEmailTokenSlot: CapturingSlot<TEmailToken> = slot()

        afterContainer {
            tUserSlot.clear()
            tEmailTokenSlot.clear()
        }

        val user = TUser(
            id = "0",
            email = "do.judo1224@gmail.com",
            nickName = "do.judo1224@gmail.com",
            pass = "",
            verified = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isPublic = false
        )

        // method: verifyEmail
        Given("이메일 링크를 통해 인증하려는데") {
            When("유효하지 않은 토큰이라면") {
                every {
                    tEmailTokenRepository.findByIdAndExpirationDateAfterAndExpired(
                        any(),
                        any(),
                        any()
                    )
                } returns null
                val exception = shouldThrow<BaseException> {
                    service.verifyEmail("")
                }
                Then("INVALID_AUTH_TOKEN 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.INVALID_AUTH_TOKEN
                }
            }
            When("유효한 토큰이지만, 등록된 유저정보가 없다면") {
                every {
                    tEmailTokenRepository.findByIdAndExpirationDateAfterAndExpired(
                        any(),
                        any(),
                        any()
                    )
                } returns TEmailToken(null, "0", false, LocalDateTime.now().plusHours(1))
                every { tUserRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.verifyEmail("")
                }
                Then("INVALID_AUTH_TOKEN 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.INVALID_AUTH_TOKEN
                }
            }
            When("유효한 토큰이고 등록된 인증정보가 있다면") {
                every {
                    tEmailTokenRepository.findByIdAndExpirationDateAfterAndExpired(
                        any(),
                        any(),
                        any()
                    )
                } returns TEmailToken(null, "0", false, LocalDateTime.now().plusHours(1))
                every { tUserRepository.findById(any()) } returns Optional.of(user)
                every { tUserRepository.save(capture(tUserSlot)) } answers {
                    this.value
                }
                every { tEmailTokenRepository.save(capture(tEmailTokenSlot)) } answers {
                    this.value
                }
                service.verifyEmail("")
                Then("유저 인증 정보를 갱신하고 이메일 토큰을 사용 완료로 변경") {
                    tUserSlot.isCaptured shouldBe true
                    tUserSlot.captured.verified shouldBe true
                    tEmailTokenSlot.isCaptured shouldBe true
                    tEmailTokenSlot.captured.expired shouldBe true
                }
            }
        }
    }
}
