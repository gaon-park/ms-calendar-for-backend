package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.MagicVariables.CAN_SEARCH_START_MINUS_MONTH
import com.maple.herocalendarforbackend.dto.response.CubeCount
import com.maple.herocalendarforbackend.dto.response.CubeEventRecordResponse
import com.maple.herocalendarforbackend.dto.response.CubeHistoryResponse
import com.maple.herocalendarforbackend.dto.response.CubeOverviewResponse
import com.maple.herocalendarforbackend.dto.response.GradeUpDashboard
import com.maple.herocalendarforbackend.dto.response.ItemCount
import com.maple.herocalendarforbackend.dto.response.WholeRecordDashboardResponse
import com.maple.herocalendarforbackend.entity.ICubeTypeCount
import com.maple.herocalendarforbackend.repository.TCubeApiKeyRepository
import com.maple.herocalendarforbackend.repository.TCubeCountHistoryRepository
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import kotlin.math.roundToInt

@Suppress("LongParameterList", "ComplexMethod", "MagicNumber")
@Service
class DashboardService(
    private val tCubeHistoryRepository: TCubeHistoryRepository,
    private val tCubeApiKeyRepository: TCubeApiKeyRepository,
    private val tCubeCountHistoryRepository: TCubeCountHistoryRepository,
) {

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

        return GradeUpDashboard.convert(
            actualRed = if (redAll != 0L && redUp != 0L) (redUp.toDouble()
                .div(redAll) * 100000).roundToInt() / 1000.0 else 0.0,
            actualBlack = if (blackAll != 0L && blackUp != 0L) (blackUp.toDouble()
                .div(blackAll) * 100000).roundToInt() / 1000.0 else 0.0,
            actualAdditional = if (additionalAll != 0L && additionalUp != 0L) (additionalUp.toDouble()
                .div(additionalAll) * 100000).roundToInt() / 1000.0 else 0.0,
            nextGrade
        )
    }

    fun getCubeOverview(loginUserId: String?): CubeOverviewResponse {
        val cubeCounts = tCubeCountHistoryRepository.findAllCubeCount(loginUserId ?: "")
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
