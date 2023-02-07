package com.maple.herocalendarforbackend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableWebMvc
@Configuration
class SwaggerConfig : WebMvcConfigurer {
    @Bean
    fun allAPI(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("All API")
            .pathsToMatch("/**")
            .build()
    }

    @Bean
    fun calendarAPI(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Calendar CURD")
            .pathsToMatch("/api/schedule")
            .pathsToMatch("/api/schedule/**")
            .build()
    }

    @Bean
    fun userAPI(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("For Login User")
            .pathsToMatch("/api/user")
            .pathsToMatch("/api/user/**")
            .build()
    }

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchema = "JWT_AUTH"
        return OpenAPI()
            .addServersItem(Server().url("/"))
            .addSecurityItem(
                SecurityRequirement()
                    .addList(securitySchema)
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchema, SecurityScheme()
                            .name(securitySchema)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .info(
                Info()
                    .title("Hero Calendar API")
                    .description("Web Calendar Info")
                    .version("v0.0.1")
            )
    }
}
