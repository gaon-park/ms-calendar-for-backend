package com.maple.heroforbackend.api

import com.maple.heroforbackend.code.BaseResponseCode
import com.maple.heroforbackend.dto.request.FriendAddRequest
import com.maple.heroforbackend.exception.BaseException
import com.maple.heroforbackend.service.AccountService
import com.maple.heroforbackend.service.FriendshipService
import com.maple.heroforbackend.service.JwtAuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user/friend")
class UserFriendController(
    private val jwtAuthService: JwtAuthService,
    private val accountService: AccountService,
    private val friendshipService: FriendshipService
) {

    @PostMapping("/add")
    fun addFriend(
        request: HttpServletRequest,
        @Valid @RequestBody requestBody: FriendAddRequest
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            friendshipService.friendRequest(it, requestBody.personalKey)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    @GetMapping("/accept")
    fun friendRequestAccept(
        request: HttpServletRequest,
        @RequestParam(name = "from") requesterId: String
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            friendshipService.friendRequestAccept(requesterId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }

    @GetMapping("/refuse")
    fun friendRequestRefuse(
        request: HttpServletRequest,
        @RequestParam(name = "from") requesterId: String
    ): ResponseEntity<String> {
        accountService.findByEmail(jwtAuthService.getUserName(request))?.let {
            friendshipService.friendRequestRefuse(requesterId, it)
            return ResponseEntity.ok("ok")
        }
        throw BaseException(BaseResponseCode.BAD_REQUEST)
    }
}
