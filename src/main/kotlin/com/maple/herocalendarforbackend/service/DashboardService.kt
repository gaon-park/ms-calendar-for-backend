package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.CubeCount
import com.maple.herocalendarforbackend.dto.response.CubeEventRecordResponse
import com.maple.herocalendarforbackend.dto.response.CubeHistoryResponse
import com.maple.herocalendarforbackend.dto.response.CubeItemData
import com.maple.herocalendarforbackend.dto.response.CubeOverviewResponse
import com.maple.herocalendarforbackend.dto.response.GradeUpDashboard
import com.maple.herocalendarforbackend.dto.response.WholeRecordDashboardResponse
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Date
import java.time.LocalDate
import java.time.Period
import kotlin.math.roundToInt

@Suppress("LongParameterList", "ComplexMethod", "MagicNumber")
@Service
class DashboardService(
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
) {

    fun getGradeUpDashboard(
        loginUserId: String?,
        item: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): GradeUpDashboard {
        val start = startDate ?: LocalDate.now().minusMonths(3)
        val end = endDate ?: LocalDate.now()

        val allCount = tCubeHistoryRepository.findAllCubeCountForItemUpgrade(loginUserId ?: "", item ?: "", start, end)
        val gradeUpCount = tCubeHistoryRepository.findItemUpgradeCount(loginUserId ?: "", item ?: "", start, end)

        val redAll = allCount.firstOrNull { it.getCubeType() == "레드 큐브" }?.getCount() ?: 0
        val blackAll = allCount.firstOrNull { it.getCubeType() == "블랙 큐브" }?.getCount() ?: 0
        val additionalAll = allCount.firstOrNull { it.getCubeType() == "에디셔널 큐브" }?.getCount() ?: 0

        val redUp = gradeUpCount.firstOrNull { it.getCubeType() == "레드 큐브" }?.getCount() ?: 0
        val blackUp = gradeUpCount.firstOrNull { it.getCubeType() == "블랙 큐브" }?.getCount() ?: 0
        val additionalUp = gradeUpCount.firstOrNull { it.getCubeType() == "에디셔널 큐브" }?.getCount() ?: 0

        return GradeUpDashboard(
            actualRed = if (redAll != 0L && redUp != 0L) (redUp.toDouble()
                .div(redAll) * 100000).roundToInt() / 1000.0 else 0.0,
            actualBlack = if (blackAll != 0L && blackUp != 0L) (blackUp.toDouble()
                .div(blackAll) * 100000).roundToInt() / 1000.0 else 0.0,
            actualAdditional = if (additionalAll != 0L && additionalUp != 0L) (additionalUp.toDouble()
                .div(additionalAll) * 100000).roundToInt() / 1000.0 else 0.0
        )
    }

    fun getCubeOverview(loginUserId: String?): CubeOverviewResponse {
        val cubeCounts = tCubeHistoryRepository.findCubeTypeCount(loginUserId ?: "")
        return CubeOverviewResponse(
            registeredApiKeyCount = if (loginUserId != null) null else tCubeApiKeyRepository.count(),
            counts = CubeCount.convert(cubeCounts),
        )
    }

    fun getWholeRecordDashboard(
        loginUserId: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): WholeRecordDashboardResponse {
        val start = startDate ?: LocalDate.of(2022, 11, 25)
        val end = endDate ?: LocalDate.now()
        val period = Period.between(start, end)

        val res = when {
            period.months > 3 -> {
                val tmp =
                    tCubeHistoryRepository.findWholeRecordDashboardMonth(loginUserId ?: "", start, end)
                        .groupBy { "${it.getYear()}/${it.getMonth()}" }
                tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }
            }
            else -> {
                val sub = tCubeHistoryRepository.findWholeRecordDashboardDate(loginUserId ?: "", start, end)
                    .groupBy { it.getDate() }
                sub.keys.mapNotNull {
                    sub[it]?.let { it1 -> CubeEventRecordResponse.convertDate(it1) }
                }
            }
        }.flatten()

        return WholeRecordDashboardResponse(
            categories = res.map { it.category }.toSet().toList(),
            data = res
        )
    }

    fun getItemFilterOption(loginUserId: String): List<String> {
        return tCubeHistoryRepository.findItemFilterOption(loginUserId)
    }

    fun itemHistorySearch(
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
                tCubeHistoryRepository.findHistoryByCondition(
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
            else tCubeHistoryRepository.findHistoryOrderByCreatedAt(loginUserId)
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
