package com.maple.herocalendarforbackend

import com.maple.herocalendarforbackend.batch.ExpiredDataDeleteScheduler
import com.maple.herocalendarforbackend.batch.ReloadAvatarImgScheduler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@Suppress("UnusedPrivateMember")
@SpringBootApplication
class HeroCalendarForBackendApplication(
	private val expiredDataDeleteScheduler: ExpiredDataDeleteScheduler,
	private val reloadAvatarImgScheduler: ReloadAvatarImgScheduler
)

fun main(args: Array<String>) {
	runApplication<HeroCalendarForBackendApplication>(args = args)
}
