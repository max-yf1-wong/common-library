package com.pccw.ruby.common.config;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalDateTime;

@EnableOpenApi
@Configuration
@ConditionalOnMissingBean(CommonSwaggerConfig.class)
public class CommonSwaggerConfig {

    @Value("${swagger.enabled:false}")
    protected boolean isEnabled;

    @Value("${swagger.host:''}")
    protected String host;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(Operation.class))
                .build()
                .useDefaultResponseMessages(false)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .enable(isEnabled)
                .host(host);
    }
}
