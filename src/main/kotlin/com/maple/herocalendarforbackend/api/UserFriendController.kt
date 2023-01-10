package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.dto.request.FriendRequest
import com.maple.herocalendarforbackend.service.AccountService
import com.maple.herocalendarforbackend.service.FriendshipService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/user/friend")
class UserFriendController(
    private val accountService: AccountService,
    private val friendshipService: FriendshipService
) {

    @PostMapping("/add")
    fun addFriend(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendAddRequest
    ): ResponseEntity<String> =
        accountService.findById(principal.name).let {
            friendshipService.friendRequest(it, requestBody)
            ResponseEntity.ok("ok")
        }

    @GetMapping("/accept")
    fun friendRequestAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ): ResponseEntity<String> =
        accountService.findById(principal.name).let {
            friendshipService.friendRequestAccept(requestBody.personalKey, it)
            ResponseEntity.ok("ok")
        }

    @GetMapping("/refuse")
    fun friendRequestRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ): ResponseEntity<String> =
        accountService.findById(principal.name).let {
            friendshipService.friendRequestRefuse(requestBody.personalKey, it)
            return ResponseEntity.ok("ok")
        }
}
