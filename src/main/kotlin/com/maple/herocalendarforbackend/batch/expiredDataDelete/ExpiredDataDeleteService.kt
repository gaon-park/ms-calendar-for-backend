package com.maple.herocalendarforbackend.batch.expiredDataDelete

import com.maple.herocalendarforbackend.repository.TJwtAuthRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberGroupRepository
import com.maple.herocalendarforbackend.repository.TScheduleMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Suppress("TooGenericExceptionCaught")
@Service
class ExpiredDataDeleteService(
    private val tJwtAuthRepository: TJwtAuthRepository,
    private val tScheduleMemberGroupRepository: TScheduleMemberGroupRepository,
    private val tScheduleMemberRepository: TScheduleMemberRepository,
) {
    private val logger = LoggerFactory.getLogger(ExpiredDataDeleteService::class.java)

    @Transactional
    fun deleteExpired() {
        logger.info("DB 처리 시작")
        val now = LocalDateTime.now()
        tJwtAuthRepository.deleteExpired(now)
        tScheduleMemberGroupRepository.findUnusedGroupIds().let {
            if (it.isNotEmpty()) {
                tScheduleMemberRepository.deleteByGroupKeyGroupIdIn(it.mapNotNull { g -> g.id })
                tScheduleMemberGroupRepository.deleteAll(it)
            }
        }

        logger.info("DB 처리 종료")
    }
}
