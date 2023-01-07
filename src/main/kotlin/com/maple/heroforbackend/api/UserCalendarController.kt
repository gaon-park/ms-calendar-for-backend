package com.maple.heroforbackend.api

import com.maple.heroforbackend.dto.request.ScheduleAddRequest
import com.maple.heroforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.heroforbackend.dto.request.ScheduleOwnerChangeRequest
import com.maple.heroforbackend.dto.request.ScheduleUpdateRequest
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.JwtAuthService
import com.maple.heroforbackend.service.ScheduleService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/calendar")
class UserCalendarController(
    private val jwtAuthService: JwtAuthService,
    private val accountService: AccountService,
    private val scheduleService: ScheduleService,
) {

    /**
     * 스케줄 입력
     */
    @PostMapping("/schedule")
    fun addSchedule(
        request: HttpServletRequest,
        @Valid @RequestBody data: ScheduleAddRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.save(it, data)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 삭제
     * 0. 주최자인 경우, 파티 전체 삭제
     * 1. 참석자인 경우, 파티 탈주로 간주
     */
    @DeleteMapping("/schedule/{scheduleId}")
    fun deleteSchedule(
        request: HttpServletRequest,
        @PathVariable(name = "scheduleId") scheduleId: Long,
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.delete(scheduleId, it)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 수정(멤버 추가)
     */
    @PutMapping("/schedule/members/{scheduleId}")
    fun putScheduleMember(
        request: HttpServletRequest,
        @PathVariable(name = "scheduleId") scheduleId: Long,
        @Valid @RequestBody requestBody: ScheduleMemberAddRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.updateMember(scheduleId, it, requestBody)
        }
        return ResponseEntity.ok("ok")
    }

    @PutMapping("/schedule/{scheduleId}/owner-change")
    fun ownerChangeRequest(
        request: HttpServletRequest,
        @PathVariable(name = "scheduleId") scheduleId: Long,
        @Valid @RequestBody requestBody: ScheduleOwnerChangeRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.changeOwnerRequest(scheduleId, it, requestBody.nextOwnerEmail)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 소유자 변경 요청 수락
     */
    @GetMapping("/schedule/{scheduleId}/owner-change/accept")
    fun ownerChangeAccept(
        request: HttpServletRequest,
        @PathVariable(name = "scheduleId") scheduleId: Long,
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.changeOwnerAccept(scheduleId, it)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 소유자 변경 요청 거절
     */
    @GetMapping("/schedule/{scheduleId}/owner-change/refuse")
    fun ownerChangeRefuse(
        request: HttpServletRequest,
        @PathVariable(name = "scheduleId") scheduleId: Long,
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.changeOwnerRefuse(scheduleId, it)
        }
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 수정(누구든 참석자인 경우 조정 가능)
     */
    @PutMapping("/schedule/{scheduleId}")
    fun putSchedule(
        request: HttpServletRequest,
        @PathVariable(name = "scheduleId") scheduleId: Long,
        @Valid @RequestBody requestBody: ScheduleUpdateRequest,
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.update(scheduleId, it, requestBody)
        }
        return ResponseEntity.ok("ok")
    }
}
