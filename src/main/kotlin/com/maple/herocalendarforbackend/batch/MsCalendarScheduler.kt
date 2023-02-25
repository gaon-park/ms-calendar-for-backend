package com.maple.herocalendarforbackend.batch

import com.maple.herocalendarforbackend.batch.expiredDataDelete.ExpiredDataDeleteJob
import com.maple.herocalendarforbackend.batch.nexon.GetCubeHistoryJob
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
class MsCalendarScheduler(
    private val applicationContext: ApplicationContext
) {
    private val schedulerFactory: SchedulerFactory = StdSchedulerFactory()
    private val scheduler: Scheduler = schedulerFactory.scheduler

    @PostConstruct
    fun expiredDataDeleteBat() {
        scheduler.start()

        val factoryBean = JobDetailFactoryBean()

        factoryBean.setJobClass(ExpiredDataDeleteJob::class.java)
        factoryBean.setName("만료 데이터 삭제")
        factoryBean.setDescription("만료되었거나 사용되지 않는 데이터를 삭제합니다")
        factoryBean.setApplicationContext(applicationContext)
        factoryBean.setApplicationContextJobDataKey("applicationContext")
        factoryBean.afterPropertiesSet()

        val job = factoryBean.`object`

        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(
                CronScheduleBuilder
                    .cronSchedule("0 30 5 * * ?")
            )
            .build()

        scheduler.scheduleJob(job, trigger)
    }

    @PostConstruct
    fun cubeHistoryGetBat() {
        scheduler.start()
        val factoryBean = JobDetailFactoryBean()
        factoryBean.setJobClass(GetCubeHistoryJob::class.java)
        factoryBean.setName("큐브 데이터 수집 배치")
        factoryBean.setDescription("넥슨 서버에서 큐브 데이터를 수집합니다")
        factoryBean.setApplicationContext(applicationContext)
        factoryBean.setApplicationContextJobDataKey("applicationContext")
        factoryBean.afterPropertiesSet()

        val job = factoryBean.`object`

        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(
                CronScheduleBuilder
                    .cronSchedule("0 19 5 * * ?")
            )
            .build()

        scheduler.scheduleJob(job, trigger)
    }
}
