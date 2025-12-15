package com.example.be.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("TICKETBLITZ API")
            .version("1.0")
            .description("콘서트 티켓팅 서비스 API"));
  }

  // Auth 도메인
  @Bean
  public GroupedOpenApi authApi() {
    return GroupedOpenApi.builder()
        .group("1. Auth")
        .pathsToMatch("/auth/**")
        .build();
  }

  // User 도메인
  @Bean
  public GroupedOpenApi userApi() {
    return GroupedOpenApi.builder()
        .group("2. User")
        .pathsToMatch("/users/**")
        .build();
  }

  // Concert 도메인
  @Bean
  public GroupedOpenApi concertApi() {
    return GroupedOpenApi.builder()
        .group("3. Concert")
        .pathsToMatch("/concerts/**")
        .build();
  }
}
