package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.nexon.CubeType
import com.maple.herocalendarforbackend.code.nexon.PotentialOption
import com.maple.herocalendarforbackend.entity.TCubeApiKey
import com.maple.herocalendarforbackend.entity.TCubeHistory
import com.maple.herocalendarforbackend.entity.TCubeHistoryBatch
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryBatchRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import com.maple.herocalendarforbackend.util.NexonUtil
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("MagicNumber")
@Service
class CubeService(
    private val userService: UserService,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeHistoryBatchRepository: TCubeHistoryBatchRepository,
) {

    private val potentialOptionMap = PotentialOption.values().associateBy { it.value }
    private val cubeTypeMap = CubeType.values().associateBy { it.type }

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
        startDate.datesUntil(today).parallel().forEach {
            saveHistory(loginUserId, apiKey, it)
            batchDateList.add(it)
        }

        saveKey(apiKey, loginUserId)
        saveBatchKey(batchDateList, loginUserId)
    }

    @Transactional
    fun saveKey(apiKey: String, loginUserId: String) {
        tCubeApiKeyRepository.save(TCubeApiKey(loginUserId, apiKey, false))
    }

    @Transactional
    fun saveBatchKey(batchDateList: List<LocalDate>, loginUserId: String) {
        tCubeHistoryBatchRepository.saveAllAndFlush(batchDateList.map { TCubeHistoryBatch.convert(loginUserId, it) })
    }

    @Transactional
    fun saveHistory(loginUserId: String, apiKey: String, date: LocalDate) {
        val nexonUtil = NexonUtil()
        val data = nexonUtil.firstProcess(apiKey, date.toString())
        if (data.count != null && data.cubeHistories.isNotEmpty()) {
            tCubeHistoryRepository.saveAll(
                data.cubeHistories.map {
                    TCubeHistory.convert(loginUserId, it, cubeTypeMap, potentialOptionMap)
                }
            )
        }
        var nextCursor = data.nextCursor
        while (nextCursor.isNotEmpty()) {
            val inData = nexonUtil.whileProcess(nextCursor, apiKey)
            tCubeHistoryRepository.saveAll(
                inData.cubeHistories.map { history ->
                    TCubeHistory.convert(loginUserId, history, cubeTypeMap, potentialOptionMap)
                }
            )
            nextCursor = inData.nextCursor
        }
    }
}
