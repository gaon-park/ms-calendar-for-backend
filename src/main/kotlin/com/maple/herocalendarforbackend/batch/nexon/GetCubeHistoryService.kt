package com.maple.herocalendarforbackend.batch.nexon

import com.auth0.jwt.JWT
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.nexon.CubeType
import com.maple.herocalendarforbackend.code.nexon.PotentialOption
import com.maple.herocalendarforbackend.entity.TCubeApiKey
import com.maple.herocalendarforbackend.entity.TCubeHistory
import com.maple.herocalendarforbackend.entity.TCubeHistoryBatch
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryBatchRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import com.maple.herocalendarforbackend.util.NexonUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Suppress("NestedBlockDepth", "MagicNumber")
@Service
class GetCubeHistoryService(
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeHistoryBatchRepository: TCubeHistoryBatchRepository
) {
    private val potentialOptionMap = PotentialOption.values().associateBy { it.value }
    private val cubeTypeMap = CubeType.values().associateBy { it.type }

    private val logger = LoggerFactory.getLogger(GetCubeHistoryService::class.java)

    fun process() {
        logger.info("Nexon CubeHistory API 처리 시작")
        val count = tCubeApiKeyRepository.count()
        val limit = 1000
        var offset = 0L
        for (i in 0..count step 1000) {
            offset += i
            val apiKey = tCubeApiKeyRepository.findByLimitOffset(limit, offset).associateBy { it.userId }
            val batchDate = tCubeHistoryBatchRepository.findByUserIdInLast(apiKey.keys.toList())
            batchDate.forEach { bat ->
                val userId = bat.batchKey.userId
                val batchDateList = mutableListOf<LocalDate>()
                val startDate = bat.batchKey.batchDate.plusDays(1)
                val today = LocalDate.now()

                apiKey[userId]?.let { key ->
                    startDate.datesUntil(today).parallel().forEach { date ->
                        saveHistory(userId, key.apiKey, date)
                        batchDateList.add(date)
                    }
                    saveKeyAndBatchKey(key.apiKey, userId, batchDateList)
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
    fun saveHistory(userId: String, apiKey: String, date: LocalDate) {
        val nexonUtil = NexonUtil()
        val data = nexonUtil.firstProcess(apiKey, date.toString())
        if (data.count != null && data.cubeHistories.isNotEmpty()) {
            tCubeHistoryRepository.saveAll(
                data.cubeHistories.map {
                    TCubeHistory.convert(userId, it, cubeTypeMap, potentialOptionMap)
                }
            )
        }
        var nextCursor = data.nextCursor
        while (nextCursor.isNotEmpty()) {
            val inData = nexonUtil.whileProcess(nextCursor, apiKey)
            tCubeHistoryRepository.saveAll(
                inData.cubeHistories.map { history ->
                    TCubeHistory.convert(userId, history, cubeTypeMap, potentialOptionMap)
                }
            )
            nextCursor = inData.nextCursor
        }
    }
}
