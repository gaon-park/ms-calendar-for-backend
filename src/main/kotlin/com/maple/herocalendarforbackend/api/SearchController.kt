package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.service.SearchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
class SearchController(
    private val searchService: SearchService
) {

    /**
     * email/nickName 으로 publicUser 검색
     */
    @GetMapping("/user")
    fun findPublicByEmailOrNickName(@RequestParam(name = "user") user: String): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(
            searchService.findPublicByEmailOrNickName(user).map {
                UserResponse(it.email, it.nickName)
            }
        )
}
