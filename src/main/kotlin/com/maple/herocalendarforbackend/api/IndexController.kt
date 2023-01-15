package com.maple.herocalendarforbackend.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Deprecated("")
@Controller
@Suppress("FunctionOnlyReturningConstant")
class IndexController {
    @GetMapping("/oauth2/index")
    fun index(): String {
        return "index"
    }

    @GetMapping("/oauth2/redirect")
    fun redirect(): String {
        return "redirect"
    }
}
