package com.maple.herocalendarforbackend.batch.nexon

import com.maple.herocalendarforbackend.code.MagicVariables.CAN_SEARCH_START_MINUS_MONTH
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Suppress("MagicNumber")
@Service
class ExpiredCubeHistoryDeleteService(
    private val tCubeHistoryRepository: TCubeHistoryRepository,
) {

    private val logger = LoggerFactory.getLogger(ExpiredCubeHistoryDeleteService::class.java)

    fun process() {
        logger.info("Expired CubeHistory Delete 처리 시작")

        tCubeHistoryRepository.deleteByCreatedAt(LocalDate.now().minusMonths(CAN_SEARCH_START_MINUS_MONTH).minusDays(1))

        logger.info("Expired CubeHistory Delete 처리 종료")
    }
}
