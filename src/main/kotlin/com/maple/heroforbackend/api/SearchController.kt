package com.maple.heroforbackend.api

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.response.UserResponse
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.JwtAuthService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
class SearchController(
    private val jwtAuthService: JwtAuthService,
    private val accountService: AccountService
) {

    /**
     * email/nickName 으로 publicUser 검색
     */
    @GetMapping("/user")
    fun findPublicByEmailOrNickName(@RequestParam(name = "user") user: String): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(
            accountService.findPublicByEmailOrNickName(user).map {
                UserResponse(it.email, it.nickName)
            }
        )

    /**
     * 로그인 유저의 친구 리스트 검색
     * 비로그인 유저의 경우, 예외 발생 없이 빈 리스트 반환
     */
    @GetMapping("/friends")
    fun findFriends(
        request: HttpServletRequest
    ): ResponseEntity<List<UserResponse>> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            return ResponseEntity.ok(
                it.friends.map { f ->
                    UserResponse(
                        email = if (f.key.requester.id == it.id) f.key.respondent.email else it.email,
                        nickName = if (f.key.requester.id == it.id) f.key.respondent.nickName else it.nickName
                    )
                }
            )
        }
        return ResponseEntity.ok(emptyList())
    }
}
