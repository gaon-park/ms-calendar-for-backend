package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val tUserRepository: TUserRepository
) {
    fun findPublicByEmailOrNickName(user: String): List<TUser> =
        tUserRepository.findByEmailOrNickNameAndIsPublic(user, true)
}
