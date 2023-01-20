package com.maple.herocalendarforbackend.api

import com.maple.herocalendarforbackend.api.base.ExceptionHandler
import com.maple.herocalendarforbackend.config.JwtAuthenticationFilter
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.service.JwtAuthService
import com.maple.herocalendarforbackend.service.SearchService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.nio.charset.StandardCharsets

class SearchControllerTest : DescribeSpec() {
    init {
        val searchService = mockk<SearchService>()
        val jwtAuthService = mockk<JwtAuthService>(relaxed = true)
        val baseUri = "/search"
        afterContainer {
            clearAllMocks()
        }

        val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
            SearchController(searchService)
        ).setControllerAdvice(ExceptionHandler())
            .defaultResponseCharacterEncoding<StandaloneMockMvcBuilder>(StandardCharsets.UTF_8)
            .addFilters<StandaloneMockMvcBuilder>(JwtAuthenticationFilter(jwtAuthService))
            .build()

        describe("search publicUser") {
            val perform = {
                mockMvc.perform(
                    MockMvcRequestBuilders.get("$baseUri/user")
                        .servletPath("$baseUri/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("user", "user")
                        .accept(MediaType.APPLICATION_JSON)
                )
            }
            context("no result") {
                every { searchService.findPublicByEmailOrNickName(any()) } returns emptyList()
                val result = perform()
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
            context("some results") {
                every { searchService.findPublicByEmailOrNickName(any()) } returns listOf()
                val result = perform()
                it("정상 종료") {
                    result.andExpect {
                        it.response.status shouldBe HttpStatus.OK.value()
                    }
                }
            }
        }
    }
}
