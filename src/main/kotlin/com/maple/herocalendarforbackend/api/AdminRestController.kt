package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.dto.request.admin.DateRequest
import com.maple.herocalendarforbackend.service.CubeService
import com.maple.herocalendarforbackend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/admin", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdminRestController(
    private val tUserService: UserService,
    private val cubeService: CubeService
) {

    @PostMapping("/batch/re-run")
    fun dataProblemSolve(
        principal: Principal,
        @Valid @RequestBody requestBody: DateRequest
    ) {
        if (tUserService.findById(principal.name).role == "ADMIN") {
            cubeService.dataProblemSolve(requestBody.date)
        }
    }
}
