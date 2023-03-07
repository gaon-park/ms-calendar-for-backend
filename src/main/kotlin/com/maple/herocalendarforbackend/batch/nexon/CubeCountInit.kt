package com.maple.herocalendarforbackend.batch.nexon

import com.maple.herocalendarforbackend.code.MagicVariables.CAN_SEARCH_START_MINUS_MONTH
import com.maple.herocalendarforbackend.entity.TCubeCountHistory
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeCountHistoryRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate

@Suppress("MagicNumber")
@Component
class CubeCountInit(
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeCountHistoryRepository: TCubeCountHistoryRepository,
) {
    private val logger = LoggerFactory.getLogger(CubeCountInit::class.java)

    @PostConstruct
    fun init() {
        if (tCubeCountHistoryRepository.count() < 1) {
            logger.info("이미 등록된 cubeHistory 데이터에서 cubeCount 추출하여 DB 입력")

            val count = tCubeApiKeyRepository.count()
            val limit = 100
            var offset = 0L

            for (i in 0..count step 100) {
                offset += i
                val apiKey = tCubeApiKeyRepository.findByLimitOffset(limit, offset).associateBy { it.userId }
                val ccMap =
                    tCubeHistoryRepository.findCubeCountForBatch(apiKey.keys.toList()).groupBy { it.getUserId() }
                val entities = mutableListOf<TCubeCountHistory>()
                ccMap.keys.map {
                    ccMap[it]?.let { list ->
                        entities.addAll(
                            list.map { it1 ->
                                TCubeCountHistory.convert(
                                    userId = it1.getUserId(),
                                    date = it1.getCreatedAt(),
                                    cc = TCubeCountHistory.CubeCountUpgrade(
                                        potentialOptionGrade = it1.getPotentialOptionGrade(),
                                        additionalPotentialOptionGrade = it1.getAdditionalPotentialOptionGrade(),
                                        targetItem = it1.getTargetItem(),
                                        cubeType = it1.getCubeType(),
                                        count = it1.getCount(),
                                        upgradeCount = it1.getItemUpgradeCount()
                                    )
                                )
                            }
                        )
                    }
                }
                tCubeCountHistoryRepository.saveAll(entities)
            }

            logger.info("cubeCount 데이터 입력 완료")
        }

        logger.info("init: Expired CubeHistory Delete 처리 시작")
        tCubeHistoryRepository.deleteByCreatedAt(LocalDate.now().minusMonths(CAN_SEARCH_START_MINUS_MONTH))
        logger.info("init: Expired CubeHistory Delete 처리 종료")
    }
}
