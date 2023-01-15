package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserService(
    private val tUserRepository: TUserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails? = username?.let {
        tUserRepository.findByEmail(it)
    }

    fun findById(id: String): TUser =
        tUserRepository.findById(id).let {
            if (it.isEmpty) throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            it.get()
        }
}
