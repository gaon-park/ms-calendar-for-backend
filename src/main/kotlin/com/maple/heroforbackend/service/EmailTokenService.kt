package com.maple.heroforbackend.service

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.entity.TEmailToken
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.repository.TEmailTokenRepository
import com.maple.heroforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EmailTokenService(
    private val emailSendService: EmailSendService,
    private val tEmailTokenRepository: TEmailTokenRepository,
    private val tUserRepository: TUserRepository,
) {

    fun sendEmailToken(userId: Long, to: String): String? {
        val emailToken = TEmailToken.generate(userId)
        tEmailTokenRepository.save(emailToken)

        emailSendService.sendEmail(emailToken, to)
        return emailToken.id
    }

    fun findByIdAndExpirationDateAfterAndExpired(token: String): TEmailToken =
        tEmailTokenRepository.findByIdAndExpirationDateAfterAndExpired(token, LocalDateTime.now(), false)
            ?: throw BaseException(BaseResponseCode.INVALID_AUTH_TOKEN)

    @Transactional
    fun verifyEmail(token: String): TUser {
        val tEmailToken = findByIdAndExpirationDateAfterAndExpired(token)
        val user = tUserRepository.findById(tEmailToken.userId)

        if (user.isPresent) {
            val userData = tUserRepository.save(user.get().copy(verified = true))
            tEmailTokenRepository.save(tEmailToken.setTokenToUsed())
            return userData
        } else {
            throw BaseException(BaseResponseCode.INVALID_AUTH_TOKEN)
        }
    }
}
