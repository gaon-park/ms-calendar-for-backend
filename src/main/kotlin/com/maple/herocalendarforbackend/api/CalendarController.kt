package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleDeleteRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.PersonalScheduleResponse
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.service.OfficialScheduleService
import com.maple.herocalendarforbackend.service.ScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.LocalDate

@Tag(name = "Calendar CURD", description = "Schedule作成、更新、閲覧、削除関連 API")
@RestController
@RequestMapping("/api/schedule", produces = [MediaType.APPLICATION_JSON_VALUE])
class CalendarController(
    private val scheduleService: ScheduleService,
    private val officialScheduleService: OfficialScheduleService,
) {

    /**
     * 스케줄 입력
     */
    @Operation(summary = "save schedule", description = "スケジュール作成 API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PostMapping
    fun addSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleAddRequest
    ): ResponseEntity<String> {
        if (requestBody.forOfficial == true) {
            officialScheduleService.save(principal.name, requestBody)
        } else {
            scheduleService.save(principal.name, requestBody)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 삭제
     * 0. 소유자인 경우, 파티 폭발
     * 1. 참석자인 경우, 단지 파티 탈주
     */
    @Operation(
        summary = "delete schedule", description = "スケジュール削除 API <br>" +
                "- RequesterがSchedule Ownerである場合: ScheduleUpdateCodeによってスケジュール自体を削除<br>" +
                "- RequesterがSchedule Participantである場合: 個人のカレンダーからだけ削除"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/delete")
    fun deleteSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleDeleteRequest
    ): ResponseEntity<String> {
        if (requestBody.forOfficial == true) {
            officialScheduleService.delete(principal.name, requestBody)
        } else {
            scheduleService.delete(principal.name, requestBody)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 수정(누구든 참석자인 경우 조정 가능)
     */
    @Operation(
        summary = "update schedule",
        description = "Schedule内容を更新する API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping
    fun putSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleUpdateRequest,
    ): ResponseEntity<String> {
        if (requestBody.forOfficial) {
            officialScheduleService.update(principal.name, requestBody)
        } else {
            scheduleService.update(principal.name, requestBody)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 추가 요청 수락
     */
    @Operation(summary = "invite accept", description = "Schedule招待を受け取る API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/accept")
    fun scheduleAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.inviteAccept(requestBody.scheduleId, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 추가 요청 거절
     */
    @Operation(summary = "invite refuse", description = "Schedule招待を断る API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            ),
            ApiResponse(
                responseCode = "401",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/refuse")
    fun scheduleRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.inviteRefuse(requestBody.scheduleId, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 검색
     * 로그인 유저: 게임 내 공식 스케줄 + 등록 스케줄
     * 비로그인 유저: 게임 내 공식 스케줄
     */
    @Operation(summary = "get schedules", description = "期間中の全てのScheduleを取得する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                content = arrayOf(
                    Content(schema = Schema(implementation = ScheduleResponse::class))
                )
            ),
            ApiResponse(
                responseCode = "400",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping
    fun getSchedules(
        principal: Principal?,
        @RequestParam from: LocalDate,
        @RequestParam to: LocalDate
    ): ResponseEntity<ScheduleResponse> {
        val officials = officialScheduleService.find(from, to)
        val personals = principal?.name?.let { scheduleService.findForPersonal(it, from, to) } ?: emptyList()
        return ResponseEntity.ok(ScheduleResponse(officials, personals))
    }

    @GetMapping("/other")
    fun getSchedulesOthers(
        principal: Principal?,
        @RequestParam userId: String,
        @RequestParam from: LocalDate,
        @RequestParam to: LocalDate
    ): ResponseEntity<List<PersonalScheduleResponse>> {
        return ResponseEntity.ok(scheduleService.findForOther(principal?.name, userId, from, to))
    }
}
