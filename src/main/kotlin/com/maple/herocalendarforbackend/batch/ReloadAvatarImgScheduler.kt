package com.maple.herocalendarforbackend.batch

import jakarta.annotation.PostConstruct
import org.quartz.CronScheduleBuilder
import org.quartz.Scheduler
import org.quartz.SchedulerFactory
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.stereotype.Component

@Component
class ReloadAvatarImgScheduler(
    private val applicationContext: ApplicationContext
) {
    private val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
    private val scheduler: Scheduler = schedulerFactory.scheduler

    @PostConstruct
    fun start() {
        scheduler.start()

        val factoryBean = JobDetailFactoryBean()

        factoryBean.setJobClass(ReloadAvatarImgJob::class.java)
        factoryBean.setName("아바타 이미지 최신화")
        factoryBean.setDescription("Maple.gg 로부터 최신 아바타 이미지를 받아옵니다")
        factoryBean.setApplicationContext(applicationContext)
        factoryBean.setApplicationContextJobDataKey("applicationContext")
        factoryBean.afterPropertiesSet()

        val job = factoryBean.`object`

        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(
                CronScheduleBuilder
                    .cronSchedule("0 30 6 * * ?")
            )
            .build()

        scheduler.scheduleJob(job, trigger)
    }
}
