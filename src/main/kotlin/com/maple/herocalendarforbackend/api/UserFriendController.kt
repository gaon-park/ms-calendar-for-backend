package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.FriendAddRequest
import com.maple.herocalendarforbackend.dto.request.FriendRequest
import com.maple.herocalendarforbackend.dto.response.UserResponse
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
    private val friendshipService: FriendshipService
) {

    /**
     * 친구 요청 보내기
     */
    @PostMapping("/add")
    fun addFriend(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendAddRequest
    ): ResponseEntity<String> {
        friendshipService.friendRequest(principal.name, requestBody)
        return ResponseEntity.ok("ok")
    }

    /**
     * 친구 요청 수락
     */
    @GetMapping("/accept")
    fun friendRequestAccept(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ): ResponseEntity<String> {
        friendshipService.friendRequestAccept(requestBody.personalKey, principal.name)
        return ResponseEntity.ok("ok")
    }

    /**
     * 친구 요청 거절
     */
    @GetMapping("/refuse")
    fun friendRequestRefuse(
        principal: Principal,
        @Valid @RequestBody requestBody: FriendRequest
    ): ResponseEntity<String> {
        friendshipService.friendRequestRefuse(requestBody.personalKey, principal.name)
        return ResponseEntity.ok("ok")
    }


    /**
     * 로그인 유저의 친구 리스트
     */
    @GetMapping
    fun findFriends(
        principal: Principal
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            friendshipService.findFriends(principal.name).map {
                UserResponse(it.email, it.nickName)
            }
        )
    }
}
