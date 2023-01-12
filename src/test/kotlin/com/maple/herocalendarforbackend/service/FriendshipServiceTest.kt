package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.AcceptedStatus
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.entity.TFriendship
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import java.util.*

class FriendshipServiceTest : BehaviorSpec() {
    init {
        val tUserRepository = mockk<TUserRepository>()
        val tFriendshipRepository = mockk<TFriendshipRepository>()
        val emailSendService = mockk<EmailSendService>()
        val service = FriendshipService(tUserRepository, tFriendshipRepository)

        val keySlot: CapturingSlot<List<TFriendship.Key>> = slot()
        val friendshipSlot: CapturingSlot<TFriendship> = slot()

        beforeContainer {
            every { tFriendshipRepository.save(capture(friendshipSlot)) } answers {
                this.value
            }
            every { tFriendshipRepository.delete(capture(friendshipSlot)) } answers {
                this.value
            }
        }

        afterContainer {
            keySlot.clear()
            friendshipSlot.clear()
            clearAllMocks()
        }

        val requester = TUser(
            id = "0",
            email = "do.judo1224@gmail.com",
            nickName = "",
            pass = "",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isPublic = false
        )

        val respondent = TUser(
            id = "1",
            email = "do.judo1224@gmail.com",
            nickName = "",
            pass = "",
            verified = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isPublic = false
        )

        // method: friendRequest
        Given("친구 요청을 보내는데") {
            When("본인이 본인에게 요청한다면") {
                val exception = shouldThrow<BaseException> {
                    service.friendRequest(requester.id!!, FriendAddRequest(requester.id!!, null))
                }
                Then("BAD_REQUEST 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                }
            }
            When("요청 대상을 찾을 수 없다면") {
                every { tUserRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.friendRequest(requester.id!!, FriendAddRequest(respondent.id!!, null))
                }
                Then("USER_NOT_FOUND 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.USER_NOT_FOUND
                }
            }
            When("이미 응답자에게 요청을 받은 상태라면") {
                every { tUserRepository.findById(any()) } returns Optional.of(respondent)
                every { tFriendshipRepository.findByKeyIn(capture(keySlot)) } returns
                        TFriendship(
                            TFriendship.Key(
                                respondent, requester
                            ), "", AcceptedStatus.WAITING, LocalDateTime.now(), LocalDateTime.now()
                        )
                service.friendRequest(requester.id!!, FriendAddRequest(respondent.id!!, null))
                Then("친구 관계를 수락") {
                    keySlot.isCaptured shouldBe true
                    friendshipSlot.isCaptured shouldBe true
                    friendshipSlot.captured.acceptedStatus shouldBe AcceptedStatus.ACCEPTED
                }
            }
            When("이미 요청자가 요청후, 응답을 대기중인 상태라면") {
                every { tUserRepository.findById(any()) } returns Optional.of(respondent)
                every { tFriendshipRepository.findByKeyIn(capture(keySlot)) } returns
                        TFriendship(
                            TFriendship.Key(
                                requester, respondent
                            ), "", AcceptedStatus.WAITING, LocalDateTime.now(), LocalDateTime.now()
                        )
                val exception = shouldThrow<BaseException> {
                    service.friendRequest(requester.id!!, FriendAddRequest(respondent.id!!, null))
                }
                Then("WAITING_FOR_RESPONDENT 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.WAITING_FOR_RESPONDENT
                }
            }
            When("아무런 관계가 없는 상태라면") {
                every { tUserRepository.findById(any()) } returns Optional.of(respondent)
                every { tFriendshipRepository.findByKeyIn(capture(keySlot)) } returns null
                every { emailSendService.sendFriendRequestEmail(any(), any()) } just Runs
                service.friendRequest(requester.id!!, FriendAddRequest(respondent.id!!, null))
                Then("요청을 진행") {
                    friendshipSlot.isCaptured shouldBe true
                    friendshipSlot.captured.acceptedStatus shouldBe AcceptedStatus.WAITING
                }
            }
        }

        // method: friendRequestAccept
        Given("친구 요청을 승인하려는데") {
            When("요청자를 찾을 수 없다면") {
                every { tUserRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.friendRequestAccept(requester.id!!, respondent.id!!)
                }
                Then("USER_NOT_FOUND 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.USER_NOT_FOUND
                }
            }
            When("요청 기록이 없다면") {
                every { tUserRepository.findById(any()) } returns Optional.of(requester)
                every { tFriendshipRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.friendRequestAccept(requester.id!!, respondent.id!!)
                }
                Then("BAD_REQUEST 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                }
            }
            When("정상 요청이라면") {
                every { tUserRepository.findById(any()) } returns Optional.of(requester)
                every { tFriendshipRepository.findById(any()) } returns Optional.of(
                    TFriendship(
                        TFriendship.Key(
                            requester, respondent
                        ), "", AcceptedStatus.WAITING, LocalDateTime.now(), LocalDateTime.now()
                    )
                )
                service.friendRequestAccept(requester.id!!, respondent.id!!)
                Then("승인 처리") {
                    friendshipSlot.isCaptured shouldBe true
                    friendshipSlot.captured.key.also {
                        it.requester.id shouldBe requester.id
                        it.respondent.id shouldBe respondent.id
                    }
                    friendshipSlot.captured.acceptedStatus shouldBe AcceptedStatus.ACCEPTED
                }
            }
        }
        // method: friendRequestRefuse
        Given("친구 요청을 거절하려는데") {
            When("요청자를 찾을 수 없다면") {
                every { tUserRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.friendRequestRefuse(requester.id!!, respondent.id!!)
                }
                Then("USER_NOT_FOUND 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.USER_NOT_FOUND
                }
            }
            When("요청 기록이 없다면") {
                every { tUserRepository.findById(any()) } returns Optional.of(requester)
                every { tFriendshipRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.friendRequestRefuse(requester.id!!, respondent.id!!)
                }
                Then("BAD_REQUEST 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                }
            }
            When("정상 요청이라면") {
                every { tUserRepository.findById(any()) } returns Optional.of(requester)
                every { tFriendshipRepository.findById(any()) } returns Optional.of(
                    TFriendship(
                        TFriendship.Key(
                            requester, respondent
                        ), "", AcceptedStatus.WAITING, LocalDateTime.now(), LocalDateTime.now()
                    )
                )
                service.friendRequestRefuse(requester.id!!, respondent.id!!)
                Then("거절 처리") {
                    friendshipSlot.isCaptured shouldBe true
                    friendshipSlot.captured.key.also {
                        it.requester.id shouldBe requester.id
                        it.respondent.id shouldBe respondent.id
                    }
                    friendshipSlot.captured.acceptedStatus shouldBe AcceptedStatus.REFUSED
                }
            }
        }

        // method: deleteFriend
        Given("친구를 삭제하려는데") {
            When("요청자나 상대방을 찾을 수 없다면") {
                every { tUserRepository.findById(any()) } returns Optional.ofNullable(null)
                val exception = shouldThrow<BaseException> {
                    service.friendRequestRefuse(requester.id!!, respondent.id!!)
                }
                Then("USER_NOT_FOUND 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.USER_NOT_FOUND
                }
            }
            When("친구가 아니라면") {
                every { tUserRepository.findById(any()) } returns Optional.of(requester)
                every { tFriendshipRepository.findById(any()) } throws BaseException(BaseResponseCode.BAD_REQUEST)
                val exception = shouldThrow<BaseException> {
                    service.friendRequestRefuse(requester.id!!, respondent.id!!)
                }
                Then("BAD_REQUEST 예외 발생") {
                    exception.errorCode shouldBe BaseResponseCode.BAD_REQUEST
                }
            }
            When("정상 요청") {
                every { tUserRepository.findById(any()) } returns Optional.of(requester)
                every { tFriendshipRepository.findById(any()) } returns Optional.of(
                    TFriendship(
                        TFriendship.Key(requester, respondent),
                        "", AcceptedStatus.ACCEPTED, LocalDateTime.now(), LocalDateTime.now()
                    )
                )
                every { tFriendshipRepository.delete(capture(friendshipSlot)) } just Runs
                service.friendRequestRefuse(requester.id!!, respondent.id!!)
                Then("정상 종료") {
                    friendshipSlot.isCaptured shouldBe true
                }
            }
        }
    }
}
