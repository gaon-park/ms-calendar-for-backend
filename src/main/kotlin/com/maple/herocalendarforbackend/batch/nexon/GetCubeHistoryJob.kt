package com.maple.herocalendarforbackend.batch.nexon

import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
class GetCubeHistoryJob: QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(GetCubeHistoryJob::class.java)

    override fun executeInternal(context: JobExecutionContext) {
        logger.info(context.jobDetail.description)
        val ctx = context.jobDetail.jobDataMap["applicationContext"] as ApplicationContext
        val service = ctx.getBean(GetCubeHistoryService::class.java)
        service.process()
        logger.info("작업 종료")
    }
}
