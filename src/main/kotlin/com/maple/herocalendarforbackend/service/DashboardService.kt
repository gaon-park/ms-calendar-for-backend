package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.CubeCount
import com.maple.herocalendarforbackend.dto.response.CubeEventRecordResponse
import com.maple.herocalendarforbackend.dto.response.CubeHistoryResponse
import com.maple.herocalendarforbackend.dto.response.CubeItemData
import com.maple.herocalendarforbackend.dto.response.CubeOverviewResponse
import com.maple.herocalendarforbackend.dto.response.WholeRecordDashboardResponse
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Suppress("LongParameterList", "ComplexMethod", "MagicNumber")
@Service
class DashboardService(
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
) {

    fun getCubeOverview(loginUserId: String?): CubeOverviewResponse {
        val cubeCounts = if (loginUserId != null) tCubeHistoryRepository.findCubeTypeCountPersonal(loginUserId)
        else tCubeHistoryRepository.findCubeTypeCountCommon()
        return CubeOverviewResponse(
            registeredApiKeyCount = if (loginUserId != null) null else tCubeApiKeyRepository.count(),
            counts = CubeCount.convert(cubeCounts),
        )
    }

    fun getWholeRecordDashboardPersonal(
        loginUserId: String,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): WholeRecordDashboardResponse {
        val start = startDate ?: LocalDate.of(2022, 11, 25)
        val end = endDate ?: LocalDate.now()
        val period = Period.between(start, end)

        val res = when {
            period.months > 12 -> {
                val tmp =
                    tCubeHistoryRepository.findWholeRecordDashboardMonthPersonal(loginUserId, end.minusMonths(10), end)
                        .groupBy { "${it.getYear()}/${it.getMonth()}" }
                tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }
            }
            period.months == 12 -> {
                val tmp = tCubeHistoryRepository.findWholeRecordDashboardMonthPersonal(loginUserId, start, end)
                    .groupBy { "${it.getYear()}/${it.getMonth()}" }
                tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }
            }
            else -> {
                val tmp = tCubeHistoryRepository.findWholeRecordDashboardMonthPersonal(loginUserId, start, end)
                    .groupBy { "${it.getYear()}/${it.getMonth()}" }
                val list = tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }

                val sub = tCubeHistoryRepository.findWholeRecordDashboardDatePersonal(loginUserId, start, end)
                    .groupBy { it.getDate() }
                val subList = sub.keys.mapNotNull {
                    sub[it]?.let { it1 -> CubeEventRecordResponse.convertDate(it1) }
                }

                listOf(list, subList).flatten()
            }
        }.flatten()

        return WholeRecordDashboardResponse(
            categories = res.map { it.category }.toSet().toList(),
            data = res
        )
    }

    fun getWholeRecordDashboard(startDate: LocalDate?, endDate: LocalDate?): WholeRecordDashboardResponse {
        val start = startDate ?: LocalDate.of(2022, 11, 25)
        val end = endDate ?: LocalDate.now()
        val period = Period.between(start, end)

        val res = when {
            period.months > 12 -> {
                val tmp = tCubeHistoryRepository.findWholeRecordDashboardMonth(end.minusMonths(10), end)
                    .groupBy { "${it.getYear()}/${it.getMonth()}" }
                tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }
            }
            period.months == 12 -> {
                val tmp = tCubeHistoryRepository.findWholeRecordDashboardMonth(start, end)
                    .groupBy { "${it.getYear()}/${it.getMonth()}" }
                tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }
            }
            else -> {
                val tmp = tCubeHistoryRepository.findWholeRecordDashboardMonth(start, end)
                    .groupBy { "${it.getYear()}/${it.getMonth()}" }
                val list = tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }

                val sub = tCubeHistoryRepository.findWholeRecordDashboardDate(start, end).groupBy { it.getDate() }
                val subList = sub.keys.mapNotNull {
                    sub[it]?.let { it1 -> CubeEventRecordResponse.convertDate(it1) }
                }

                listOf(list, subList).flatten()
            }
        }.flatten()

        return WholeRecordDashboardResponse(
            categories = res.map { it.category }.toSet().toList(),
            data = res
        )
    }

    fun getItemFilterOptionCommon(): List<String> {
        return tCubeHistoryRepository.findItemFilterOptionCommon()
    }

    fun getItemFilterOptionPersonal(loginUserId: String): List<String> {
        return tCubeHistoryRepository.findItemFilterOptionPersonal(loginUserId)
    }

    fun getItemDashboardPersonal(
        loginUserId: String,
        item: String?,
        cube: String?,
        option1: String?,
        option2: String?,
        option3: String?,
        optionValue1: Int?,
        optionValue2: Int?,
        optionValue3: Int?
    ): List<CubeHistoryResponse> {
        val history =
            if (haveCondition(item, cube, option1, option2, option3, optionValue1, optionValue2, optionValue3))
                tCubeHistoryRepository.findHistoryByConditionPersonal(
                    loginUserId,
                    item ?: "",
                    cube ?: "",
                    option1 ?: "",
                    option2 ?: "",
                    option3 ?: "",
                    optionValue1 ?: 0,
                    optionValue2 ?: 0,
                    optionValue3 ?: 0,
                )
            else tCubeHistoryRepository.findHistoryByPersonalOrderByCreatedAt(loginUserId)
        return history.map {
            CubeHistoryResponse.convert(it)
        }
    }

    fun getItemDashboard(
        item: String?,
        cube: String?,
        option1: String?,
        option2: String?,
        option3: String?,
        optionValue1: Int?,
        optionValue2: Int?,
        optionValue3: Int?
    ): List<CubeHistoryResponse> {
        val history =
            if (haveCondition(item, cube, option1, option2, option3, optionValue1, optionValue2, optionValue3))
                tCubeHistoryRepository.findHistoryByCondition(
                    item ?: "",
                    cube ?: "",
                    option1 ?: "",
                    option2 ?: "",
                    option3 ?: "",
                    optionValue1 ?: 0,
                    optionValue2 ?: 0,
                    optionValue3 ?: 0,
                )
            else tCubeHistoryRepository.findHistoryByPersonalOrderByCreatedAt()
        return history.map {
            CubeHistoryResponse.convert(it)
        }
    }

    fun haveCondition(
        item: String?,
        cube: String?,
        option1: String?,
        option2: String?,
        option3: String?,
        optionValue1: Int?,
        optionValue2: Int?,
        optionValue3: Int?
    ): Boolean {
        return when {
            !item.isNullOrEmpty() -> true
            !cube.isNullOrEmpty() -> true
            !option1.isNullOrEmpty() -> true
            !option2.isNullOrEmpty() -> true
            !option3.isNullOrEmpty() -> true
            optionValue1 != null -> true
            optionValue2 != null -> true
            optionValue3 != null -> true
            else -> false
        }
    }
}
