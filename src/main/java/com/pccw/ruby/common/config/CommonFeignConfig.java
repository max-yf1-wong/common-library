package com.pccw.ruby.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@Order
@ConditionalOnMissingBean(CommonFeignConfig.class)
public class CommonFeignConfig {

    @SuppressWarnings("rawtypes")
    @Bean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory =
                () -> new HttpMessageConverters(jacksonConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public Encoder feignEncoder(ObjectMapper objectMapper) {
        HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory =
                () -> new HttpMessageConverters(jacksonConverter);
        return new SpringEncoder(objectFactory);
    }
}
