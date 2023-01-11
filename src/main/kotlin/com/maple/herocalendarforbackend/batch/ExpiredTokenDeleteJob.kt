package com.maple.herocalendarforbackend.batch

import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
class ExpiredTokenDeleteJob : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(ExpiredTokenDeleteJob::class.java)

    override fun executeInternal(context: JobExecutionContext) {
        logger.info(context.jobDetail.description)
        val ctx = context.jobDetail.jobDataMap["applicationContext"] as ApplicationContext
        val service = ctx.getBean(ExpiredTokenDeleteService::class.java)
        service.deleteExpired()
        logger.info("작업 종료")
    }
}
