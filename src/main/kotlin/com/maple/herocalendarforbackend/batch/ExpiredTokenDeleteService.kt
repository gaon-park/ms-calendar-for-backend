package com.maple.herocalendarforbackend.batch

import com.maple.herocalendarforbackend.repository.TEmailTokenRepository
import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Suppress("TooGenericExceptionCaught")
@Service
class ExpiredTokenDeleteService(
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tEmailTokenRepository: TEmailTokenRepository
) {
    private val logger = LoggerFactory.getLogger(ExpiredTokenDeleteService::class.java)

    @Transactional
    fun deleteExpired() {
        logger.info("DB 처리 시작")
        val now = LocalDateTime.now()
        try {
            val jwt = tJwtAuthRepository.findExpired(now)
            if (jwt.isNotEmpty()) {
                tJwtAuthRepository.deleteAll(jwt)
                logger.info("${jwt.size}개의 jwtAuth token 데이터를 삭제했습니다.")
            }
            val email = tEmailTokenRepository.findExpired(now)
            if (email.isNotEmpty()) {
                tEmailTokenRepository.deleteAll(email)
                logger.info("${jwt.size}개의 email confirm token 데이터를 삭제했습니다.")
            }
        } catch (exception: Exception) {
            logger.error(exception.message)
        }
        logger.info("DB 처리 종료")
    }
}
