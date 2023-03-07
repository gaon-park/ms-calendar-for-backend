package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.response.CubeHistoryResponse
import com.maple.herocalendarforbackend.dto.response.CubeOverviewResponse
import com.maple.herocalendarforbackend.dto.response.GradeUpDashboard
import com.maple.herocalendarforbackend.dto.response.ItemCount
import com.maple.herocalendarforbackend.dto.response.WholeRecordDashboardResponse
import com.maple.herocalendarforbackend.service.DashboardService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.LocalDate

@Suppress("TooManyFunctions")
@RestController
@RequestMapping("/api/dashboard", produces = [MediaType.APPLICATION_JSON_VALUE])
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/top-five")
    fun getRedTopFive(
        @RequestParam("cubeType", required = true) cubeType: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<List<ItemCount>> {
        return ResponseEntity.ok(
            dashboardService.getTopFiveItem(
                loginUserId = null,
                startDate = startDate,
                endDate = endDate,
                cubeType = cubeType
            )
        )
    }

    @GetMapping("/personal/top-five")
    fun getRedTopFivePersonal(
        principal: Principal,
        @RequestParam("cubeType", required = true) cubeType: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<List<ItemCount>> {
        return ResponseEntity.ok(
            dashboardService.getTopFiveItem(
                loginUserId = principal.name,
                startDate = startDate,
                endDate = endDate,
                cubeType = cubeType
            )
        )
    }

    @GetMapping("/personal/grade-up/legendary")
    fun getGradeUpPersonalLegendary(
        principal: Principal,
        @RequestParam("item", required = false) item: String?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<GradeUpDashboard> {
        return ResponseEntity.ok(
            dashboardService.getGradeDashboardByGrade(
                loginUserId = principal.name,
                item = item,
                startDate = startDate,
                endDate = endDate,
                grade = "유니크",
                nextGrade = "레전드리"
            )
        )
    }

    @GetMapping("/personal/grade-up/unique")
    fun getGradeUpPersonalUnique(
        principal: Principal,
        @RequestParam("item", required = false) item: String?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<GradeUpDashboard> {
        return ResponseEntity.ok(
            dashboardService.getGradeDashboardByGrade(
                loginUserId = principal.name,
                item = item,
                startDate = startDate,
                endDate = endDate,
                grade = "에픽",
                nextGrade = "유니크"
            )
        )
    }

    @GetMapping("/grade-up/legendary")
    fun getGradeUpCommonLegendary(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<GradeUpDashboard> {
        return ResponseEntity.ok(
            dashboardService.getGradeDashboardByGrade(
                loginUserId = null,
                item = null,
                startDate = startDate,
                endDate = endDate,
                grade = "유니크",
                nextGrade = "레전드리"
            )
        )
    }

    @GetMapping("/grade-up/unique")
    fun getGradeUpCommonUnique(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<GradeUpDashboard> {
        return ResponseEntity.ok(
            dashboardService.getGradeDashboardByGrade(
                loginUserId = null,
                item = null,
                startDate = startDate,
                endDate = endDate,
                grade = "에픽",
                nextGrade = "유니크"
            )
        )
    }

    @GetMapping("/cube-overview")
    fun getCubeCountCommon(): ResponseEntity<CubeOverviewResponse> {
        return ResponseEntity.ok(dashboardService.getCubeOverview(null))
    }

    @GetMapping("/personal/cube-overview")
    fun getCubeCountPersonal(
        principal: Principal
    ): ResponseEntity<CubeOverviewResponse> {
        return ResponseEntity.ok(dashboardService.getCubeOverview(principal.name))
    }

    @GetMapping("/personal/item-options")
    fun getItemFilterOptions(
        principal: Principal
    ): List<String> {
        return dashboardService.getItemFilterOption(principal.name)
    }

    @GetMapping("/personal/item-options/for-search")
    fun getItemFilterOptionsForSearch(
        principal: Principal
    ): List<String> {
        return dashboardService.getItemFilterOptionByCanSearchStartDate(principal.name)
    }

    @Suppress("LongParameterList")
    @GetMapping("/personal/for-item")
    fun getDashboardDataPersonal(
        principal: Principal,
        @RequestParam("item", required = false) item: String?,
        @RequestParam("cube", required = false) cube: String?,
        @RequestParam("option1", required = false) option1: String?,
        @RequestParam("option2", required = false) option2: String?,
        @RequestParam("option3", required = false) option3: String?,
        @RequestParam("optionValue1", required = false) optionValue1: Int?,
        @RequestParam("optionValue2", required = false) optionValue2: Int?,
        @RequestParam("optionValue3", required = false) optionValue3: Int?,
    ): ResponseEntity<List<CubeHistoryResponse>> {
        return ResponseEntity.ok(
            dashboardService.itemHistorySearch(
                principal.name, item, cube, option1, option2, option3, optionValue1, optionValue2, optionValue3
            )
        )
    }

    @GetMapping("/whole-record")
    fun getWholeRecordDashboardData(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<WholeRecordDashboardResponse> {
        return ResponseEntity.ok(
            dashboardService.getWholeRecordDashboard(null, startDate, endDate)
        )
    }


    @GetMapping("/personal/whole-record")
    fun getWholeRecordDashboardDataPersonal(
        principal: Principal,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<WholeRecordDashboardResponse> {
        return ResponseEntity.ok(
            dashboardService.getWholeRecordDashboard(principal.name, startDate, endDate)
        )
    }
}
