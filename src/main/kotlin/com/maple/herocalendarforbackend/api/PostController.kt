package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.post.PostAddRequest
import com.maple.herocalendarforbackend.dto.request.post.PostRequest
import com.maple.herocalendarforbackend.dto.request.post.PostUpdateRequest
import com.maple.herocalendarforbackend.service.PostService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "Post CURD")
@RestController
@RequestMapping("/api/post", produces = [MediaType.APPLICATION_JSON_VALUE])
class PostController(
    private val postService: PostService
) {

    @Operation(summary = "create post")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PostMapping
    fun createPost(
        principal: Principal,
        @Valid @RequestBody requestBody: PostAddRequest
    ) {
        postService.save(principal.name, requestBody)
    }

    @Operation(summary = "update post note")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PutMapping
    fun updatePost(
        principal: Principal,
        @Valid @RequestBody requestBody: PostUpdateRequest
    ) {
        postService.update(principal.name, requestBody)
    }

    @Operation(summary = "delete post")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200"
            )
        ]
    )
    @PutMapping("/delete")
    fun deletePost(
        principal: Principal,
        @Valid @RequestBody requestBody: PostRequest
    ) {
        postService.delete(principal.name, requestBody)
    }
}
