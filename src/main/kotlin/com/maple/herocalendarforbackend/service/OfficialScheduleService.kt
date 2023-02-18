package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleAddRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleDeleteRequest
import com.maple.herocalendarforbackend.dto.request.schedule.ScheduleUpdateRequest
import com.maple.herocalendarforbackend.dto.response.OfficialScheduleResponse
import com.maple.herocalendarforbackend.entity.TOfficialSchedule
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TOfficialScheduleRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class OfficialScheduleService(
    private val tUserRepository: TUserRepository,
    private val tOfficialScheduleRepository: TOfficialScheduleRepository,
) {

    @Transactional
    fun save(loginUserId: String, request: ScheduleAddRequest) {
        tUserRepository.findAdminByUserId(loginUserId)?.let {
            tOfficialScheduleRepository.save(
                TOfficialSchedule.convert(request)
            )
        }
    }

    @Suppress("MagicNumber")
    @Transactional
    fun update(loginUserId: String, request: ScheduleUpdateRequest) {
        val admin = tUserRepository.findAdminByUserId(loginUserId)
        val forOfficial = request.forOfficial
        if (admin == null || !forOfficial) throw BaseException(BaseResponseCode.BAD_REQUEST)

        tOfficialScheduleRepository.findById(request.scheduleId).let {
            if (it.isPresent) {
                val entity = it.get()
                val end = if (request.allDay) LocalDateTime.of(
                    request.start.year,
                    request.start.month,
                    request.start.dayOfMonth,
                    23,
                    59
                ) else request.end ?: request.start
                tOfficialScheduleRepository.save(
                    entity.copy(
                        title = request.title,
                        start = request.start,
                        end = end,
                        allDay = request.allDay,
                        note = request.note ?: entity.note
                    )
                )
            }
        }
    }

    @Transactional
    fun delete(loginUserId: String, request: ScheduleDeleteRequest) {
        val admin = tUserRepository.findAdminByUserId(loginUserId)
        val forOfficial = request.forOfficial
        if (admin == null || forOfficial != true) throw BaseException(BaseResponseCode.BAD_REQUEST)

        tOfficialScheduleRepository.deleteById(request.scheduleId)
    }

    fun find(from: LocalDate, to: LocalDate): List<OfficialScheduleResponse> {
        return convertToResponse(tOfficialScheduleRepository.findByFromTo(from, to))
    }

    fun convertToResponse(schedules: List<TOfficialSchedule>): List<OfficialScheduleResponse> {
        return schedules.map {
            OfficialScheduleResponse.convert(it)
        }
    }
}
