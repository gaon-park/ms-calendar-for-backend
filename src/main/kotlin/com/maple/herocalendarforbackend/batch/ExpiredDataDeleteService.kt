package com.maple.herocalendarforbackend.batch

import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Suppress("TooGenericExceptionCaught")
@Service
class ExpiredDataDeleteService(
    private val tJwtAuthRepository: TJwtAuthRepository,
) {
    private val logger = LoggerFactory.getLogger(ExpiredDataDeleteService::class.java)

    @Transactional
    fun deleteExpired() {
        logger.info("DB 처리 시작")
        val now = LocalDateTime.now()
        try {
            tJwtAuthRepository.findExpired(now).let {
                tJwtAuthRepository.deleteAll(it)
            }
        } catch (exception: Exception) {
            logger.error(exception.message)
        }
        logger.info("DB 처리 종료")
    }
}
