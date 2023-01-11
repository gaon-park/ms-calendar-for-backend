package com.maple.herocalendarforbackend

import com.maple.herocalendarforbackend.batch.HeroScheduler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@Suppress("UnusedPrivateMember")
@SpringBootApplication
class HeroCalendarForBackendApplication(
	private val scheduler: HeroScheduler
)

fun main(args: Array<String>) {
	runApplication<HeroCalendarForBackendApplication>(args = args)
}
