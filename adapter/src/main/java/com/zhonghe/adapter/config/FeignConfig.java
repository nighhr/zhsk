package com.zhonghe.adapter.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${api.sign-header-value}")
    private String signHeaderValue;


    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // 记录请求头、体、响应等完整信息
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 添加Sign请求头
            requestTemplate.header("Sign", signHeaderValue);
            requestTemplate.header("Content-Type", "application/json");
            // 可以添加其他公共请求头
            // requestTemplate.header("Content-Type", "application/json");
        };
    }
}