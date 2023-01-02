package com.maple.heroforbackend.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Suppress("FunctionOnlyReturningConstant")
class IndexController {
    @GetMapping
    fun index(): String {
        return "index"
    }

    @GetMapping("/user")
    fun userTest(): String {
        return "index"
    }
}
