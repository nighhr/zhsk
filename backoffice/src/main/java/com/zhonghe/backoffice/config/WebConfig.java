package com.zhonghe.backoffice.config;

import com.zhonghe.backoffice.interceptor.JwtAuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册JWT拦截器，排除路径: /api/auth/**, /api/aiTe/**");
        registry.addInterceptor(new JwtAuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/aiTe/**",
                        "/error"
                )
                .order(1);
    }
}