package com.maple.herocalendarforbackend.batch

import jakarta.annotation.PostConstruct
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.stereotype.Component

@Component
class HeroScheduler(
    private val applicationContext: ApplicationContext
) {
    private val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
    private val scheduler: Scheduler = schedulerFactory.scheduler

    @PostConstruct
    fun start() {
        scheduler.start()

        val factoryBean = JobDetailFactoryBean()

        factoryBean.setJobClass(ExpiredTokenDeleteJob::class.java)
        factoryBean.setName("만료 토큰 삭제")
        factoryBean.setDescription("만료되었거나 유효하지 않은 토큰 데이터를 삭제합니다")
        factoryBean.setApplicationContext(applicationContext)
        factoryBean.setApplicationContextJobDataKey("applicationContext")
        factoryBean.afterPropertiesSet()

        val job = factoryBean.`object`

        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(
                CronScheduleBuilder
                    .cronSchedule("0 0 6 * * ?")
            )
            .build()

        scheduler.scheduleJob(job, trigger)
    }
}
