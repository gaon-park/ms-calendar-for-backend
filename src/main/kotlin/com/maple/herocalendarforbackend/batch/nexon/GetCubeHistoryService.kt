package com.maple.herocalendarforbackend.batch.nexon

import com.auth0.jwt.JWT
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.entity.TCubeApiKey
import com.maple.herocalendarforbackend.entity.TCubeCountHistory
import com.maple.herocalendarforbackend.entity.TCubeHistory
import com.maple.herocalendarforbackend.entity.TCubeHistoryBatch
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeCountHistoryRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryBatchRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import com.maple.herocalendarforbackend.util.NexonUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Suppress("NestedBlockDepth", "MagicNumber", "MaxLineLength")
@Service
class GetCubeHistoryService(
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeHistoryBatchRepository: TCubeHistoryBatchRepository,
    private val tCubeCountHistoryRepository: TCubeCountHistoryRepository,
) {

    private val logger = LoggerFactory.getLogger(GetCubeHistoryService::class.java)

    fun process() {
        logger.info("Nexon CubeHistory API 처리 시작")
        val count = tCubeApiKeyRepository.count()
        val limit = 1000
        val today = LocalDate.now()
        var offset = 0L
        for (i in 0..count step 1000) {
            offset += i
            val apiKey = tCubeApiKeyRepository.findByLimitOffset(limit, offset).associateBy { it.userId }
            val batchDate =
                tCubeHistoryBatchRepository.findByUserIdInLast(apiKey.keys.toList()).associateBy { it.batchKey.userId }
            batchDate.keys.map { userId ->
                val batchDateList = mutableListOf<LocalDate>()
                batchDate[userId]?.let { tCubeHistoryBatch ->
                    val startDate = tCubeHistoryBatch.batchKey.batchDate
                    apiKey[tCubeHistoryBatch.batchKey.userId]?.let { tCubeApiKey ->
                        startDate.datesUntil(today).parallel().forEach { date ->
                            saveHistory(userId, tCubeApiKey.apiKey, date, LocalDate.now().minusMonths(3))
                            batchDateList.add(date.plusDays(1))
                        }
                        saveKeyAndBatchKey(tCubeApiKey.apiKey, userId, batchDateList)
                    }
                }
            }
        }

        logger.info("Nexon CubeHistory API 처리 종료")
    }


    @Transactional
    fun saveKeyAndBatchKey(apiKey: String, userId: String, batchDateList: List<LocalDate>) {
        val nexonUtil = NexonUtil()
        if (nexonUtil.isValidToken(apiKey)) {
            val jwt = JWT.decode(apiKey)
            tCubeApiKeyRepository.save(
                TCubeApiKey(
                    userId,
                    apiKey,
                    false,
                    LocalDateTime.ofInstant(jwt.issuedAtAsInstant, ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(jwt.expiresAtAsInstant, ZoneId.systemDefault())
                )
            )
            tCubeHistoryBatchRepository.saveAllAndFlush(batchDateList.map {
                TCubeHistoryBatch.convert(
                    userId,
                    it
                )
            })
        } else throw BaseException(BaseResponseCode.INVALID_TOKEN)
    }

    @Transactional
    fun saveHistory(userId: String, apiKey: String, date: LocalDate, historySaveFrom: LocalDate) {
        val nexonUtil = NexonUtil()
        val withHistorySave = date.isAfter(historySaveFrom) || date.isEqual(historySaveFrom)
        try {
            logger.info("$userId 데이터 수집!")
            val data = nexonUtil.firstProcess(apiKey, date.toString())
            val cubeCountMap = mutableMapOf<TCubeCountHistory.FromNexonData, List<TCubeHistory>>()
            if (data.count != null && data.cubeHistories.isNotEmpty()) {
                val entities = data.cubeHistories.map {
                    TCubeHistory.convert(userId, it)
                }
                cubeCountMap.putAll(
                    entities.groupBy {
                        TCubeCountHistory.FromNexonData(
                            targetItem = it.targetItem,
                            cubeType = it.cubeType,
                            potentialOptionGrade = it.potentialOptionGrade,
                            additionalPotentialOptionGrade = it.additionalPotentialOptionGrade
                        )
                    }
                )
                if (withHistorySave)
                    tCubeHistoryRepository.saveAll(entities)
            }
            var nextCursor = data.nextCursor
            while (nextCursor.isNotEmpty()) {
                val inData = nexonUtil.whileProcess(nextCursor, apiKey)
                val entities = inData.cubeHistories.map { history ->
                    TCubeHistory.convert(userId, history)
                }
                cubeCountMap.putAll(
                    entities.groupBy {
                        TCubeCountHistory.FromNexonData(
                            targetItem = it.targetItem,
                            cubeType = it.cubeType,
                            potentialOptionGrade = it.potentialOptionGrade,
                            additionalPotentialOptionGrade = it.additionalPotentialOptionGrade
                        )
                    }
                )
                if (withHistorySave)
                    tCubeHistoryRepository.saveAll(entities)
                nextCursor = inData.nextCursor
            }
            logger.info("$userId 데이터 수집, DB 저장 완!")
            saveCubeCount(userId, date, cubeCountMap)
        } catch (_: BaseException) {
            tCubeHistoryRepository.deleteByAccount(userId)
            tCubeHistoryBatchRepository.deleteByAccount(userId)
            tCubeApiKeyRepository.deleteByAccount(userId)
        }
    }

    @Transactional
    fun saveCubeCount(
        userId: String,
        date: LocalDate,
        map: Map<TCubeCountHistory.FromNexonData, List<TCubeHistory>>
    ) {
        logger.info("$userId] $date cubeCount DB 저장!")
        val entities = map.keys.map {
            TCubeCountHistory.convertFromNextData(
                userId = userId,
                date = date,
                cc = it,
                count = map[it]!!.size,
                upgradeCount = map[it]!!.count { o -> o.itemUpgrade }
            )
        }
        tCubeCountHistoryRepository.saveAll(entities)
        logger.info("$userId] $date cubeCount DB 저장 완!")
    }
}
