package com.maple.heroforbackend.service

import com.maple.heroforbackend.dto.request.AccountRegistRequest
import com.maple.heroforbackend.entity.TUser
import com.maple.heroforbackend.exception.AlreadyExistException
import com.maple.heroforbackend.repository.TUserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val passwordEncoder: PasswordEncoder,
    private val tUserRepository: TUserRepository
) {
    @Transactional
    fun insert(request: AccountRegistRequest): TUser {
        if (tUserRepository.findByEmail(request.email) != null) {
            throw AlreadyExistException("already exist email [${request.email}]")
        }
        return tUserRepository.save(TUser.generateInsertModel(request, passwordEncoder))
    }
}
