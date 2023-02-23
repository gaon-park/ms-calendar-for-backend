package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.response.ItemDashboardResponse
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
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/dashboard", produces = [MediaType.APPLICATION_JSON_VALUE])
class DashboardController(
    private val dashboardService: DashboardService
) {

    @Suppress("LongParameterList")
    @GetMapping("/for-item")
    fun getDashboardData(
        @RequestParam("item", required = false) item: String?,
        @RequestParam("cube", required = false) cube: String?,
        @RequestParam("option1", required = false) option1: String?,
        @RequestParam("option2", required = false) option2: String?,
        @RequestParam("option3", required = false) option3: String?,
        @RequestParam("optionValue1", required = false) optionValue1: Int?,
        @RequestParam("optionValue2", required = false) optionValue2: Int?,
        @RequestParam("optionValue3", required = false) optionValue3: Int?,
    ): ResponseEntity<ItemDashboardResponse> {
        return ResponseEntity.ok(
            dashboardService.getItemDashboard(
                item, cube, option1, option2, option3, optionValue1, optionValue2, optionValue3
            )
        )
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
    ): ResponseEntity<ItemDashboardResponse> {
        return ResponseEntity.ok(
            dashboardService.getItemDashboardPersonal(
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
            dashboardService.getWholeRecordDashboard(startDate, endDate)
        )
    }


    @GetMapping("/personal/whole-record")
    fun getWholeRecordDashboardDataPersonal(
        principal: Principal,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("startDate", required = false) startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("endDate", required = false) endDate: LocalDate?,
    ): ResponseEntity<WholeRecordDashboardResponse> {
        return ResponseEntity.ok(
            dashboardService.getWholeRecordDashboardPersonal(principal.name, startDate, endDate)
        )
    }
}
