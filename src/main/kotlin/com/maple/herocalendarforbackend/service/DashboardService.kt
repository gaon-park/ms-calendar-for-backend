package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.MagicVariables.CAN_SEARCH_START_MINUS_MONTH
import com.maple.herocalendarforbackend.code.MagicVariables.MAX_SEARCH_LIMIT
import com.maple.herocalendarforbackend.dto.response.CubeCount
import com.maple.herocalendarforbackend.dto.response.CubeEventRecordResponse
import com.maple.herocalendarforbackend.dto.response.CubeHistoryResponse
import com.maple.herocalendarforbackend.dto.response.CubeOverviewResponse
import com.maple.herocalendarforbackend.dto.response.GradeUpDashboard
import com.maple.herocalendarforbackend.dto.response.ItemCount
import com.maple.herocalendarforbackend.dto.response.WholeRecordDashboardResponse
import com.maple.herocalendarforbackend.entity.ICubeTypeCount
import com.maple.herocalendarforbackend.entity.TCubeHistory
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeCountHistoryRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import kotlin.math.roundToInt

@Suppress("LongParameterList", "ComplexMethod", "MagicNumber", "TooManyFunctions")
@Service
class DashboardService(
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeCountHistoryRepository: TCubeCountHistoryRepository,
) {

    private val allStatCompareList = listOf("STR", "DEX", "INT", "LUK")

    fun getTopFiveItem(
        loginUserId: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        cubeType: String,
    ): List<ItemCount> {
        val start = startDate ?: LocalDate.now().minusMonths(CAN_SEARCH_START_MINUS_MONTH)
        val end = endDate ?: LocalDate.now()
        return tCubeCountHistoryRepository.findTopFiveItem(
            loginUserId = loginUserId ?: "",
            start = start,
            end = end,
            cubeType = cubeType
        ).map {
            ItemCount(
                item = it.getTargetItem(),
                count = it.getCount()
            )
        }.sortedByDescending { o -> o.count }
    }

    fun getAllCount(
        gradeState: List<ICubeTypeCount>,
        gradeUpgraded: List<ICubeTypeCount>,
        nextGradeUpgraded: List<ICubeTypeCount>,
        cubeType: String
    ): Long {
        val res = (gradeState.firstOrNull { it.getCubeType() == cubeType }?.getCount() ?: 0) -
                (gradeUpgraded.firstOrNull { it.getCubeType() == cubeType }?.getCount() ?: 0) +
                (nextGradeUpgraded.firstOrNull { it.getCubeType() == cubeType }?.getCount() ?: 0)

        return if (res < 0) 0 else res
    }

    fun getUpgradedCount(
        nextGradeUpgraded: List<ICubeTypeCount>,
        cubeType: String
    ): Long {
        return nextGradeUpgraded.firstOrNull { it.getCubeType() == cubeType }?.getCount() ?: 0
    }

    fun getGradeDashboardByGrade(
        loginUserId: String?,
        item: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        grade: String,
        nextGrade: String,
    ): GradeUpDashboard {
        val start = startDate ?: LocalDate.now().minusMonths(CAN_SEARCH_START_MINUS_MONTH)
        val end = endDate ?: LocalDate.now()
        val gradeState = tCubeCountHistoryRepository.findStateGradeCount(
            loginUserId = loginUserId ?: "",
            item = item ?: "",
            start = start,
            end = end,
            gradeKor = grade
        )
        val gradeUpgraded = tCubeCountHistoryRepository.findUpgradeGradeCount(
            loginUserId = loginUserId ?: "",
            item = item ?: "",
            start = start,
            end = end,
            gradeKor = grade
        )
        val nextGradeUpgraded = tCubeCountHistoryRepository.findUpgradeGradeCount(
            loginUserId = loginUserId ?: "",
            item = item ?: "",
            start = start,
            end = end,
            gradeKor = nextGrade
        )

        val redAll = getAllCount(gradeState, gradeUpgraded, nextGradeUpgraded, "레드 큐브")
        val redUp = getUpgradedCount(nextGradeUpgraded, "레드 큐브")
        val blackAll = getAllCount(gradeState, gradeUpgraded, nextGradeUpgraded, "블랙 큐브")
        val blackUp = getUpgradedCount(nextGradeUpgraded, "블랙 큐브")
        val additionalAll = getAllCount(gradeState, gradeUpgraded, nextGradeUpgraded, "에디셔널 큐브")
        val additionalUp = getUpgradedCount(nextGradeUpgraded, "에디셔널 큐브")

        val actualRed = if (redAll != 0L && redUp != 0L) (redUp.toDouble()
            .div(redAll) * 100000).roundToInt() / 1000.0 else 0.0
        val actualBlack = if (blackAll != 0L && blackUp != 0L) (blackUp.toDouble()
            .div(blackAll) * 100000).roundToInt() / 1000.0 else 0.0
        val actualAdditional = if (additionalAll != 0L && additionalUp != 0L) (additionalUp.toDouble()
            .div(additionalAll) * 100000).roundToInt() / 1000.0 else 0.0

        return when (nextGrade) {
            "레전드리" -> GradeUpDashboard.convertLegendary(actualRed, actualBlack, actualAdditional)
            else -> GradeUpDashboard.convertUnique(actualRed, actualBlack, actualAdditional)
        }
    }

    fun getCubeOverview(
        loginUserId: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): CubeOverviewResponse {
        val start = startDate ?: LocalDate.of(2022, 11, 25)
        val end = endDate ?: LocalDate.now()
        val cubeCounts = tCubeCountHistoryRepository.findAllCubeCount(loginUserId ?: "", start, end)
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
        val start = startDate ?: LocalDate.now().minusMonths(CAN_SEARCH_START_MINUS_MONTH)
        val end = endDate ?: LocalDate.now()
        val period = Period.between(start, end)

        val res = when {
            period.months > 3 -> {
                val tmp =
                    tCubeCountHistoryRepository.findWholeRecordDashboardMonth(loginUserId ?: "", start, end)
                        .groupBy { "${it.getYear()}/${it.getMonth()}" }
                tmp.keys.mapNotNull {
                    tmp[it]?.let { it1 -> CubeEventRecordResponse.convertMonth(it1) }
                }
            }
            else -> {
                val sub = tCubeCountHistoryRepository.findWholeRecordDashboardDate(loginUserId ?: "", start, end)
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
        return tCubeCountHistoryRepository.findItemFilterOption(loginUserId)
    }

    fun getItemFilterOptionByCanSearchStartDate(loginUserId: String): List<String> {
        return tCubeCountHistoryRepository.findItemFilterOptionByCanSearchStartDate(
            loginUserId, LocalDate.now().minusMonths(
                CAN_SEARCH_START_MINUS_MONTH
            )
        )
    }

    fun itemHistorySearch(
        loginUserId: String,
        item: String?,
        cube: String?,
        option1Param: String?,
        option2Param: String?,
        option3Param: String?,
        optionValue1Param: Int?,
        optionValue2Param: Int?,
        optionValue3Param: Int?
    ): List<CubeHistoryResponse> {
        val option1 = option1Param ?: ""
        val option2 = option2Param ?: ""
        val option3 = option3Param ?: ""
        val optionValue1 = optionValue1Param ?: 0
        val optionValue2 = optionValue2Param ?: 0
        val optionValue3 = optionValue3Param ?: 0
        val history =
            if (haveSQLCondition(item, cube, option1, option2, option3)) {
                val tmp = tCubeHistoryRepository.findHistoryByOption(
                    userId = loginUserId,
                    item ?: "",
                    cube ?: "",
                    option1,
                    option2,
                    option3,
                ).mapNotNull {
                    if (sumMoreThanCompare(option1, optionValue1, it) &&
                        sumMoreThanCompare(option2, optionValue2, it) &&
                        sumMoreThanCompare(option3, optionValue3, it)
                    ) {
                        it
                    } else null
                }
                tmp.subList(0, MAX_SEARCH_LIMIT.toInt().coerceAtMost(tmp.size))
            } else tCubeHistoryRepository.findHistoryOrderByCreatedAt(loginUserId)
        return history.map {
            CubeHistoryResponse.convert(it)
        }
    }

    private fun sumMoreThanCompare(
        compareOption: String,
        compareOptionValue: Int,
        history: TCubeHistory
    ): Boolean {
        var sum = 0
        if (history.cubeType == "에디셔널 큐브") {
            if (compareOption == history.afterAdditionalOption1 || isAllStat(
                    compareOption,
                    history.afterAdditionalOption1 ?: ""
                )
            ) sum += toIntFromPercentValue(
                history.afterAdditionalOptionValue1 ?: "0"
            )
            if (compareOption == history.afterAdditionalOption2 || isAllStat(
                    compareOption,
                    history.afterAdditionalOption2 ?: ""
                )
            ) sum += toIntFromPercentValue(
                history.afterAdditionalOptionValue2 ?: "0"
            )
            if (compareOption == history.afterAdditionalOption3 || isAllStat(
                    compareOption,
                    history.afterAdditionalOption3 ?: ""
                )
            ) sum += toIntFromPercentValue(
                history.afterAdditionalOptionValue3 ?: "0"
            )
        } else {
            if (compareOption == history.afterOption1 || isAllStat(
                    compareOption,
                    history.afterOption1 ?: ""
                )
            ) sum += toIntFromPercentValue(
                history.afterOptionValue1 ?: "0"
            )
            if (compareOption == history.afterOption2 || isAllStat(
                    compareOption,
                    history.afterOption2 ?: ""
                )
            ) sum += toIntFromPercentValue(
                history.afterOptionValue2 ?: "0"
            )
            if (compareOption == history.afterOption3 || isAllStat(
                    compareOption,
                    history.afterOption3 ?: ""
                )
            ) sum += toIntFromPercentValue(
                history.afterOptionValue3 ?: "0"
            )
        }

        return sum >= compareOptionValue
    }

    private fun isAllStat(
        compareOption: String,
        historyOption: String
    ): Boolean {
        return allStatCompareList.any { it == compareOption } && historyOption == "올스탯"
    }

    private fun toIntFromPercentValue(
        value: String
    ): Int {
        return Integer.valueOf(value.replace("%", "").replace(" ", ""))
    }

    private fun haveSQLCondition(
        item: String?,
        cube: String?,
        option1: String?,
        option2: String?,
        option3: String?,
    ): Boolean {
        return when {
            !item.isNullOrEmpty() -> true
            !cube.isNullOrEmpty() -> true
            !option1.isNullOrEmpty() -> true
            !option2.isNullOrEmpty() -> true
            !option3.isNullOrEmpty() -> true
            else -> false
        }
    }
}
