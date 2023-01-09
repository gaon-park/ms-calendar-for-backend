package com.maple.herocalendarforbackend.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Suppress("FunctionOnlyReturningConstant")
class IndexController {
    @GetMapping("/index")
    fun index(): ResponseEntity<String> {
        return ResponseEntity.ok("ok")
    }

    @GetMapping("/user")
    fun userTest(): String {
        return "index"
    }
}
