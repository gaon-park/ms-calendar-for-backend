package com.maple.herocalendarforbackend.batch.nexon

import com.auth0.jwt.JWT
import com.maple.herocalendarforbackend.code.BaseResponseCode
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

@Suppress("NestedBlockDepth", "MagicNumber", "MaxLineLength")
@Service
class GetCubeHistoryService(
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeHistoryBatchRepository: TCubeHistoryBatchRepository
) {

    private val logger = LoggerFactory.getLogger(GetCubeHistoryService::class.java)

    fun process() {
        logger.info("Nexon CubeHistory API 처리 시작")
//        val count = tCubeApiKeyRepository.count()
//        val limit = 1000
//        val today = LocalDate.now()
//        var offset = 0L
//        for (i in 0..count step 1000) {
//            offset += i
//            val apiKey = tCubeApiKeyRepository.findByLimitOffset(limit, offset).associateBy { it.userId }
//            val batchDate =
//                tCubeHistoryBatchRepository.findByUserIdInLast(apiKey.keys.toList()).associateBy { it.batchKey.userId }
//            batchDate.keys.map { userId ->
//                val batchDateList = mutableListOf<LocalDate>()
//                batchDate[userId]?.let { tCubeHistoryBatch ->
//                    val startDate = tCubeHistoryBatch.batchKey.batchDate
//                    apiKey[tCubeHistoryBatch.batchKey.userId]?.let { tCubeApiKey ->
//                        startDate.datesUntil(today).parallel().forEach { date ->
//                            saveHistory(userId, tCubeApiKey.apiKey, date)
//                            batchDateList.add(date.plusDays(1))
//                        }
//                        saveKeyAndBatchKey(tCubeApiKey.apiKey, userId, batchDateList)
//                    }
//                }
//            }
//        }

        tCubeApiKeyRepository.findAll().map { key ->
            val startDate = LocalDate.of(2022, 11, 25)
            val now = LocalDateTime.now()
            val today = if (now.isAfter(
                    LocalDateTime.of(
                        now.year,
                        now.month,
                        now.dayOfMonth,
                        4,
                        0
                    )
                )
            ) now.toLocalDate() else now.minusDays(1).toLocalDate()

            startDate.datesUntil(today).parallel().forEach {
                saveHistory(key.userId, key.apiKey, it)
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
                    TCubeHistory.convert(userId, it)
                }
            )
        }
        var nextCursor = data.nextCursor
        while (nextCursor.isNotEmpty()) {
            val inData = nexonUtil.whileProcess(nextCursor, apiKey)
            tCubeHistoryRepository.saveAll(
                inData.cubeHistories.map { history ->
                    TCubeHistory.convert(userId, history)
                }
            )
            nextCursor = inData.nextCursor
        }
    }
}
