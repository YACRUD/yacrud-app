package com.github.jetnet.yacrud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.HttpAuthenticationScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
@Import(SpringDataRestConfiguration.class)
public class SwaggerConfig {

    @Bean
    public Docket postsApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("com.github.jetnet.yacrud.controller"))
                .build()
                .apiInfo(apiInfo())
//                .securitySchemes(Collections.singletonList(authenticationScheme()))
                .securitySchemes(Collections.singletonList(apiKey()))
                ;
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Yet Another CRUD API Service")
                .description("CRUD APIs with Spring Reactive Elasticsearch Repository")
                .termsOfServiceUrl("https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/")
                .version("1.0").build();
    }

    private static ApiKey apiKey() {
        return new ApiKey(
                // use this name for all Controllers to protect:
                //   @ApiOperation(authorizations = {@Authorization(value = "auth")})
                "auth",
                "Authorization",
                "header");
    }

    // This only works with: DocumentationType.OAS_30,
    // but this version is buggy:
    // - no default values get displayed: https://github.com/springfox/springfox/issues/3422
    private static HttpAuthenticationScheme authenticationScheme() {
        return HttpAuthenticationScheme
                .JWT_BEARER_BUILDER
                .name("auth") // use this name for all Controllers to protect
                .description("Put only a bearer auth token here")
                .build();
    }
}