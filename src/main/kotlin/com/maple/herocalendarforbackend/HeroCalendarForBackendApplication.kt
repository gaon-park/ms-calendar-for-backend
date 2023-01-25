package com.maple.herocalendarforbackend

import com.maple.herocalendarforbackend.batch.MsCalendarScheduler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@Suppress("UnusedPrivateMember")
@SpringBootApplication
class HeroCalendarForBackendApplication(
	private val msCalendarScheduler: MsCalendarScheduler
)

fun main(args: Array<String>) {
	runApplication<HeroCalendarForBackendApplication>(args = args)
}
