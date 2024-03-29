package com.maple.herocalendarforbackend.service

import com.auth0.jwt.JWT
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.response.APIKeyResponse
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
import java.util.*

@Suppress("MagicNumber")
@Service
class CubeService(
    private val userService: UserService,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeHistoryBatchRepository: TCubeHistoryBatchRepository,
    private val tCubeCountHistoryRepository: TCubeCountHistoryRepository,
) {

    private val logger = LoggerFactory.getLogger(CubeService::class.java)

    fun getApiKey(loginUserId: String): APIKeyResponse? {
        return tCubeApiKeyRepository.findByUserId(loginUserId)?.let {
            APIKeyResponse(
                apiKey = it.apiKey,
                isValid = isValidToken(it),
                createdAt = it.createdAt,
                expiredAt = it.expiredAt
            )
        }
    }

    @Transactional
    fun isValidToken(tCubeApiKey: TCubeApiKey): Boolean {
        if (tCubeApiKey.expiredAt.isBefore(LocalDateTime.now())) {
            tCubeApiKeyRepository.save(tCubeApiKey.copy(expired = true))
            return false
        }
        return true
    }

    fun registProcess(apiKey: String, loginUserId: String) {
        userService.findById(loginUserId)
        val startDate = tCubeHistoryBatchRepository.findByUserIdLast(loginUserId)?.batchKey?.batchDate?.plusDays(1)
            ?: LocalDate.of(2022, 11, 25)

        val batchDateList = mutableListOf<LocalDate>()
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
        val nexonUtil = NexonUtil()
        val saveHistoryFrom = now.minusMonths(3).toLocalDate()
        if (nexonUtil.isValidToken(apiKey)) {
            startDate.datesUntil(today).parallel().forEach {
                saveHistory(loginUserId, apiKey, it, saveHistoryFrom)
                batchDateList.add(it)
            }

            saveKeyAndBatchKey(apiKey, loginUserId, batchDateList)
        }
    }

    @Transactional
    fun saveKeyAndBatchKey(apiKey: String, loginUserId: String, batchDateList: List<LocalDate>) {
        val nexonUtil = NexonUtil()
        if (nexonUtil.isValidToken(apiKey)) {
            val jwt = JWT.decode(apiKey)
            tCubeApiKeyRepository.save(
                TCubeApiKey(
                    loginUserId,
                    apiKey,
                    false,
                    LocalDateTime.ofInstant(jwt.issuedAtAsInstant, ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(jwt.expiresAtAsInstant, ZoneId.systemDefault())
                )
            )
            tCubeHistoryBatchRepository.saveAll(batchDateList.map {
                TCubeHistoryBatch.convert(
                    loginUserId,
                    it
                )
            })
        } else throw BaseException(BaseResponseCode.INVALID_TOKEN)
    }

    @Transactional
    fun saveHistory(loginUserId: String, apiKey: String, date: LocalDate, saveHistoryFrom: LocalDate) {
        val nexonUtil = NexonUtil()
        val withHistorySave = date.isAfter(saveHistoryFrom) || date.isEqual(saveHistoryFrom)
        try {
            logger.debug("$loginUserId] $date 데이터 수집!")
            val cubeCountMap = mutableMapOf<TCubeCountHistory.FromNexonData, List<TCubeHistory>>()
            val data = nexonUtil.firstProcess(apiKey, date.toString())
            if (data.count != null && data.cubeHistories.isNotEmpty()) {
                val entities = data.cubeHistories.map {
                    TCubeHistory.convert(loginUserId, it)
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
                    TCubeHistory.convert(loginUserId, history)
                }
                val tmp = entities.groupBy {
                    TCubeCountHistory.FromNexonData(
                        targetItem = it.targetItem,
                        cubeType = it.cubeType,
                        potentialOptionGrade = it.potentialOptionGrade,
                        additionalPotentialOptionGrade = it.additionalPotentialOptionGrade
                    )
                }
                tmp.keys.map {
                    if (cubeCountMap.containsKey(it)) {
                        cubeCountMap[it] = cubeCountMap[it]!!.plus(tmp[it]!!)
                    } else {
                        cubeCountMap[it] = tmp[it]!!
                    }
                }
                if (withHistorySave)
                    tCubeHistoryRepository.saveAll(entities)
                nextCursor = inData.nextCursor
            }
            logger.debug("$loginUserId] $date 데이터 수집, DB 저장 완!")
            saveCubeCount(loginUserId, date, cubeCountMap)
        } catch (_: BaseException) {
            tCubeHistoryRepository.deleteByAccount(loginUserId)
            tCubeHistoryBatchRepository.deleteByAccount(loginUserId)
            tCubeApiKeyRepository.deleteByAccount(loginUserId)
        }
    }

    @Transactional
    fun saveCubeCount(
        loginUserId: String,
        date: LocalDate,
        map: Map<TCubeCountHistory.FromNexonData, List<TCubeHistory>>
    ) {
        logger.debug("$loginUserId] $date cubeCount DB 저장!")
        val entities = map.keys.map {
            TCubeCountHistory.convertFromNextData(
                userId = loginUserId,
                date = date,
                cc = it,
                count = map[it]!!.size,
                upgradeCount = map[it]!!.count { o -> o.itemUpgrade }
            )
        }
        tCubeCountHistoryRepository.saveAll(entities)
        logger.debug("$loginUserId] $date cubeCount DB 저장 완!")
    }


    @Transactional
    fun dataProblemSolve(date: LocalDate) {
        tCubeCountHistoryRepository.deleteByCreatedAtByBatch(date)
        tCubeHistoryRepository.deleteByCreatedAtByBatch(date)

        logger.info("기존 데이터 삭제 완료")

        val nexonUtil = NexonUtil()
        val saveHistoryFrom = LocalDate.now().minusMonths(3)
        tCubeApiKeyRepository.findAll().map {
            if (nexonUtil.isValidToken(it.apiKey)) {
                logger.info("${it.userId} 데이터 재등록")
                saveHistory(it.userId, it.apiKey, date, saveHistoryFrom)
                logger.info("${it.userId} 데이터 재등록 완료")
            }
        }
    }
}
