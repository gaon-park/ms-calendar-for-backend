package com.maple.herocalendarforbackend.batch

import com.maple.herocalendarforbackend.code.MagicVariables.EMAIL_TOKEN_EXPIRATION_TIME_VALUE
import com.maple.herocalendarforbackend.repository.TEmailTokenRepository
import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Date

@Suppress("TooGenericExceptionCaught")
@Service
class ExpiredDataDeleteService(
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tEmailTokenRepository: TEmailTokenRepository,
    private val tUserRepository: TUserRepository,
) {
    private val logger = LoggerFactory.getLogger(ExpiredDataDeleteService::class.java)

    @Transactional
    fun deleteExpired() {
        logger.info("DB 처리 시작")
        val now = LocalDateTime.now()
        val nowDate = Date()
        try {
            tJwtAuthRepository.findExpired(now).let {
                tJwtAuthRepository.deleteAll(it)
            }
            tUserRepository.findByNotVerified(Date(nowDate.time - EMAIL_TOKEN_EXPIRATION_TIME_VALUE)).let {
                tUserRepository.deleteAll(it)
            }
            tEmailTokenRepository.findExpired(now).let {
                tEmailTokenRepository.deleteAll(it)
            }
        } catch (exception: Exception) {
            logger.error(exception.message)
        }
        logger.info("DB 처리 종료")
    }
}
