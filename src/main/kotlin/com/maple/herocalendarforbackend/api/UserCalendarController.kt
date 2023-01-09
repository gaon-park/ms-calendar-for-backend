package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleMemberAddRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleOwnerChangeRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleRequest
import com.maple.herocalendarforbackend.dto.request.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.service.ScheduleService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/schedule")
class UserCalendarController(
    private val jwtAuthService: JwtAuthService,
    private val accountService: AccountService,
    private val scheduleService: ScheduleService,
) {

    /**
     * 스케줄 입력
     */
    @PostMapping
    fun addSchedule(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleAddRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.save(it, requestBody)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 삭제
     * 0. 소유자인 경우, 파티 전체 삭제
     * 1. 참석자인 경우, 파티 탈주로 간주
     */
    @DeleteMapping
    fun deleteSchedule(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.delete(requestBody.scheduleId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 수정(멤버 추가)
     */
    @PutMapping("/members")
    fun putScheduleMember(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleMemberAddRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.updateMember(it, requestBody)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 소유자 수정 요청
     */
    @PostMapping("/owner-change")
    fun ownerChangeRequest(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleOwnerChangeRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.changeOwnerRequest(requestBody.scheduleId, it, requestBody.nextOwnerEmail)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 소유자 변경 요청 수락
     */
    @PutMapping("/owner-change/accept")
    fun ownerChangeAccept(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.changeOwnerAccept(requestBody.scheduleId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 소유자 변경 요청 거절
     */
    @PutMapping("/owner-change/refuse")
    fun ownerChangeRefuse(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.changeOwnerRefuse(requestBody.scheduleId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 수정(누구든 참석자인 경우 조정 가능)
     */
    @PutMapping
    fun putSchedule(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleUpdateRequest,
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.update(it, requestBody)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 추가 요청 수락
     */
    @PutMapping("/accept")
    fun scheduleAccept(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.scheduleAccept(requestBody.scheduleId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    /**
     * 스케줄 추가 요청 거절
     */
    @PutMapping("/refuse")
    fun scheduleRefuse(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: ScheduleRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            scheduleService.scheduleRefuse(requestBody.scheduleId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }
}
