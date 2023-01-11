package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.entity.TEmailToken
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TEmailTokenRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EmailTokenService(
    private val emailSendService: EmailSendService,
    private val tEmailTokenRepository: TEmailTokenRepository,
    private val tUserRepository: TUserRepository,
) {

    fun sendEmailToken(userId: String, to: String): String? {
        val emailToken = TEmailToken.generate(userId)
        tEmailTokenRepository.save(emailToken)

        emailSendService.sendAuthEmail(emailToken, to)
        return emailToken.id
    }

    fun findByIdAndExpirationDateAfterAndExpired(token: String): TEmailToken =
        tEmailTokenRepository.findByIdAndExpirationDateAfterAndExpired(token, LocalDateTime.now(), false)
            ?: throw BaseException(BaseResponseCode.INVALID_TOKEN)

    @Transactional
    fun verifyEmail(token: String) {
        val tEmailToken = findByIdAndExpirationDateAfterAndExpired(token)
        val user = tUserRepository.findById(tEmailToken.userId)

        if (user.isPresent) {
            tUserRepository.save(user.get().copy(verified = true))
            tEmailTokenRepository.delete(tEmailToken)
        } else {
            throw BaseException(BaseResponseCode.INVALID_TOKEN)
        }
    }
}
