package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.dto.response.CubeHistoryResponse
import com.maple.herocalendarforbackend.dto.response.ItemDashboardResponse
import com.maple.herocalendarforbackend.repository.TCubeHistoryRepository
import org.springframework.stereotype.Service

@Suppress("LongParameterList", "ComplexMethod")
@Service
class DashboardService(
    private val tCubeHistoryRepository: TCubeHistoryRepository,
) {
    fun getItemDashboard(
        item: String?,
        cube: String?,
        option1: String?,
        option2: String?,
        option3: String?,
        optionValue1: Int?,
        optionValue2: Int?,
        optionValue3: Int?
    ): ItemDashboardResponse {
        val itemList = tCubeHistoryRepository.findItemFilterOptionCommon()
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
            else tCubeHistoryRepository.findHistoryByItemIn(itemList)
        return ItemDashboardResponse(
            itemList = itemList,
            cubeHistories = history.map {
                CubeHistoryResponse.convert(it)
            }
        )
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
