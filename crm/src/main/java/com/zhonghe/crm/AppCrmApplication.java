package com.zhonghe.apporder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.zhonghe.apporder",
        "com.zhonghe.kernel",
        "com.zhonghe.adapter",
        "com.zhonghe.backoffice"
})
@EnableFeignClients(basePackages = "com.zhonghe.adapter.feign")
public class AppCrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppCrmApplication.class, args);
    }
}