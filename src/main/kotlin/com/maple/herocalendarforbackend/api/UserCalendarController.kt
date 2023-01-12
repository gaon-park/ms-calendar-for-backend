package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleGetRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleOwnerChangeRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.ErrorResponse
import com.maple.herocalendarforbackend.dto.response.ScheduleResponse
import com.maple.herocalendarforbackend.service.ScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Calendar CURD", description = "Schedule作成、更新、閲覧、削除関連 API")
@RestController
@RequestMapping("/user/schedule", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserCalendarController(
    private val scheduleService: ScheduleService,
) {

    /**
     * 스케줄 입력
     */
    @Operation(summary = "save schedule", description = "スケジュール作成 API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. スケジュール作成がメール認定してない<br>" +
                        "1. 未加入ユーザは招待できない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PostMapping
    fun addSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleAddRequest
    ) {
        scheduleService.save(principal.name, requestBody)
    }

    /**
     * 스케줄 삭제
     * 0. 소유자인 경우, 파티 전체 삭제
     * 1. 참석자인 경우, 파티 탈주로 간주
     */
    @Operation(
        summary = "delete schedule", description = "スケジュール削除 API <br>" +
                "- RequesterがSchedule Ownerである場合: 全ユーザのカレンダーから削除 <br>" +
                "- RequesterがSchedule Participantである場合: 個人のカレンダーからだけ削除"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. Scheduleが存在しない <br>" +
                        "1. RequesterがSchedule Memberじゃない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @DeleteMapping
    fun deleteSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ) {
        scheduleService.delete(requestBody.scheduleId, principal.name)
    }

    /**
     * 스케줄 수정(멤버 추가)
     */
    @Operation(summary = "invite new members", description = "スケジュール更新（メンバー追加） API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. Scheduleが存在しない <br>" +
                        "1 RequesterがSchedule Memberじゃない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/members")
    fun putScheduleMember(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleMemberAddRequest
    ) {
        scheduleService.updateMember(principal.name, requestBody)
    }

    /**
     * 소유자 수정 요청
     */
    @Operation(
        summary = "change schedule owner",
        description = "Schedule Ownerを変更するRequestを送る API <br>" +
                "*次のOwnerが受け取る前までは未確定"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. Scheduleが存在しない <br>" +
                        "1. RequesterがSchedule Ownerじゃない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/owner-change")
    fun ownerChangeRequest(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleOwnerChangeRequest
    ): ResponseEntity<String> {
        scheduleService.changeOwnerRequest(principal.name, requestBody)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 소유자 변경 요청 수락
     */
    @Operation(
        summary = "change schedule owner accept",
        description = "Schedule Owner変更Requestを受け取る API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. Scheduleが存在しない <br>" +
                        "1. Requesterが元Ownerから指名されたNext Schedule Ownerじゃない <br>" +
                        "2. ScheduleがOwner Change待機中じゃない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/owner-change/accept")
    fun ownerChangeAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ) {
        scheduleService.changeOwnerAccept(requestBody.scheduleId, principal.name)
    }

    /**
     * 스케줄 소유자 변경 요청 거절
     */
    @Operation(
        summary = "change schedule owner refuse",
        description = "Schedule Owner変更Requestを断る API"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. Scheduleが存在しない <br>" +
                        "1. Requesterが元Ownerから指名されたNext Schedule Ownerじゃない <br>" +
                        "2. ScheduleがOwner Change待機中じゃない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/owner-change/refuse")
    fun ownerChangeRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.changeOwnerRefuse(requestBody.scheduleId, principal.name)
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
                description = "0. Scheduleが存在しない <br>" +
                        "1. RequesterがSchedule Memberじゃない",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping
    fun putSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleUpdateRequest,
    ) {
        scheduleService.update(principal.name, requestBody)
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
                description = "0. 招待受けてない <br>",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/accept")
    fun scheduleAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ) {
        scheduleService.scheduleAccept(requestBody.scheduleId, principal.name)
    }

    /**
     * 스케줄 추가 요청 거절
     */
    @Operation(summary = "invite refuse", description = "Schedule招待を断る API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK"
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. 招待受けてない <br>",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @PutMapping("/refuse")
    fun scheduleRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ) {
        scheduleService.scheduleRefuse(requestBody.scheduleId, principal.name)
    }

    /**
     * 로그인 유저의 스케줄
     */
    @Operation(summary = "get schedules", description = "期間中の全てのScheduleを取得する API")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = arrayOf(
                    Content(array = ArraySchema(schema = Schema(implementation = ScheduleResponse::class)))
                )
            ),
            ApiResponse(
                responseCode = "400",
                description = "0. to日時がfromより先",
                content = arrayOf(Content(schema = Schema(implementation = ErrorResponse::class)))
            )
        ]
    )
    @GetMapping
    fun getSchedules(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleGetRequest
    ): ResponseEntity<List<ScheduleResponse>> {
        return ResponseEntity.ok(
            scheduleService.findSchedulesAndConvertToResponse(
                principal.name, requestBody
            )
        )
    }
}
