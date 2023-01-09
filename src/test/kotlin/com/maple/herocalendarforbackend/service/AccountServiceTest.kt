package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.AccountRegistRequest
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TUserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.security.crypto.password.PasswordEncoder

class AccountServiceTest : BehaviorSpec() {
    init {
        val passwordEncoder = mockk<PasswordEncoder>()
        val tUserRepository = mockk<TUserRepository>()

        val service = AccountService(
            passwordEncoder, tUserRepository
        )

        val tUserSlot: CapturingSlot<TUser> = slot()

        every { passwordEncoder.encode(any()) } answers { this.value }
        every { tUserRepository.save(capture(tUserSlot)) } answers { this.value }

        val addRequest = AccountRegistRequest(
            email = "do.judo1224@gmail.com",
            password = "qwer1234",
            confirmPassword = "qwer1234",
            nickName = null,
            isPublic = null
        )
        val user = TUser.generateSaveModel(addRequest, passwordEncoder).copy(id = "0")

        // method: findByEmail
        Given("이메일을 통해 유저를 검색하는데") {
            When("존재하지 않는 유저라면") {
                every { tUserRepository.findByEmail(any()) } answers { null }
                val result = service.findByEmail("")
                Then("null 을 반환한다") {
                    result shouldBe null
                }
            }
            When("존재하는 유저라면") {
                every { tUserRepository.findByEmail(any()) } answers { user }
                val result = service.findByEmail(user.email)
                Then("해당 유저 객체를 반환한다") {
                    result shouldBe user
                }
            }
        }

        // method: save
        Given("유저 등록 요청이 들어왔을 때") {
            When("등록된 유저의 이메일과 중복된다면") {
                every { tUserRepository.findByEmail(any()) } answers { user }
                val exception = shouldThrow<BaseException> {
                    service.save(addRequest)
                }
                Then("DUPLICATE_EMAIL 예외가 발생한다") {
                    exception.errorCode shouldBe BaseResponseCode.DUPLICATE_EMAIL
                    tUserSlot.isCaptured shouldBe false
                }
            }
            When("등록된 유저의 이메일과 중복되지 않는다면") {
                every { tUserRepository.findByEmail(any()) } answers { null }
                service.save(addRequest)
                Then("DB에 저장된다") {
                    tUserSlot.isCaptured shouldBe true
                    tUserSlot.captured.email shouldBe addRequest.email
                    tUserSlot.captured.nickName shouldBe addRequest.email
                }
            }
        }
    }
}
