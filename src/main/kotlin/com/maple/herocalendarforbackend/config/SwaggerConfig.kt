package com.maple.herocalendarforbackend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
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
            .pathsToMatch("/user/schedule")
            .pathsToMatch("/user/schedule/**")
            .build()
    }

    @Bean
    fun friendAPI(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Friend CURD")
            .pathsToMatch("/user/friend")
            .pathsToMatch("/user/friend/**")
            .build()
    }

    @Bean
    fun userAPI(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("For Login User")
            .pathsToMatch("/user")
            .pathsToMatch("/user/**")
            .build()
    }

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchema = "JWT_AUTH"
        return OpenAPI()
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
                            .scheme("Bearer")
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
