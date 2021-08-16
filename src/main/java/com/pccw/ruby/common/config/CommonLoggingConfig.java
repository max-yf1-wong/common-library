package com.pccw.ruby.common.config;

import com.pccw.ruby.common.filter.CommonLoggingFilter;
import com.pccw.ruby.common.service.LoggingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonLoggingConfig {

    @Value("${logging.com.pccw.ruby.common.filter.url-whitelist:/actuator/health}")
    private List<String> urlWhitelist;

    @Bean
    @ConditionalOnMissingBean
    public CommonLoggingFilter logFilter(LoggingService logService) {
        return new CommonLoggingFilter(urlWhitelist, logService);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingService logService() {
        return new LoggingService();
    }
}
