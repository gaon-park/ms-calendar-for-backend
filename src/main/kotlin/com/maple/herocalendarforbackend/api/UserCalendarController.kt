package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleGetRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleOwnerChangeRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/user/schedule")
class UserCalendarController(
    private val scheduleService: ScheduleService,
) {

    /**
     * 스케줄 입력
     */
    @PostMapping
    fun addSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleAddRequest
    ): ResponseEntity<String> {
        scheduleService.save(principal.name, requestBody)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 삭제
     * 0. 소유자인 경우, 파티 전체 삭제
     * 1. 참석자인 경우, 파티 탈주로 간주
     */
    @DeleteMapping
    fun deleteSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.delete(requestBody.scheduleId, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 수정(멤버 추가)
     */
    @PutMapping("/members")
    fun putScheduleMember(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleMemberAddRequest
    ): ResponseEntity<String> {
        scheduleService.updateMember(principal.name, requestBody)
        return ResponseEntity.ok("ok")
    }

    /**
     * 소유자 수정 요청
     */
    @PostMapping("/owner-change")
    fun ownerChangeRequest(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleOwnerChangeRequest
    ): ResponseEntity<String> {
        scheduleService.changeOwnerRequest(requestBody.scheduleId, principal.name, requestBody.nextOwnerEmail)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 소유자 변경 요청 수락
     */
    @PutMapping("/owner-change/accept")
    fun ownerChangeAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.changeOwnerAccept(requestBody.scheduleId, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 소유자 변경 요청 거절
     */
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
    @PutMapping
    fun putSchedule(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleUpdateRequest,
    ): ResponseEntity<String> {
        scheduleService.update(principal.name, requestBody)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 추가 요청 수락
     */
    @PutMapping("/accept")
    fun scheduleAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.scheduleAccept(requestBody.scheduleId, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 스케줄 추가 요청 거절
     */
    @PutMapping("/refuse")
    fun scheduleRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        scheduleService.scheduleRefuse(requestBody.scheduleId, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 한 달 단위 스케줄 획득
     */
    @GetMapping
    fun getSchedules(
        principal: Principal,
        @Valid @RequestBody requestBody: ScheduleGetRequest
    ): ResponseEntity<List<Any>> {
        return ResponseEntity.ok(
            scheduleService.findSchedules(
                principal.name, requestBody
            )
        )
    }

}
