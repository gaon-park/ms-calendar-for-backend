package com.maple.herocalendarforbackend.batch.reloadAvatarImg

import com.maple.herocalendarforbackend.repository.TUserRepository
import com.maple.herocalendarforbackend.util.MapleGGUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Suppress("MagicNumber")
@Service
class ReloadAvatarImgService(
    private val tUserRepository: TUserRepository,
) {
    private val mapleGGUtil = MapleGGUtil()

    @Transactional
    fun reloadAndSave() {
        var start = 0
        val size = 50
        val totalElements = tUserRepository.count()
        while (start < totalElements) {
            val page = tUserRepository.findAll(PageRequest.of(start, size, Sort.by("id")))
            val data = page.filter { it.nickName != it.email && !it.nickName.contains("@") }
                .mapNotNull {
                    val avatarImg = getAvatarImg(it.nickName)
                    if (avatarImg != null) {
                        it.copy(
                            avatarImg = avatarImg,
                            updatedAt = LocalDateTime.now()
                        )
                    } else null
                }
            tUserRepository.saveAll(data)
            start += size
        }
    }

    fun getAvatarImg(name: String): String? {
        return mapleGGUtil.getAvatarImg(name)
    }
}
