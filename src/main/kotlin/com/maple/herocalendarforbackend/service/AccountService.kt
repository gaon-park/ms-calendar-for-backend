package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.request.AccountRegistRequest
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val passwordEncoder: PasswordEncoder,
    private val tUserRepository: TUserRepository
) {
    fun findByEmail(email: String): TUser? = tUserRepository.findByEmail(email)

    @Transactional
    fun save(request: AccountRegistRequest): TUser {
        if (findByEmail(request.email) != null) {
            throw BaseException(BaseResponseCode.DUPLICATE_EMAIL)
        }
        return tUserRepository.save(TUser.generateSaveModel(request, passwordEncoder))
    }
}
