package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(
        scanBasePackages = {
                "com.zhonghe.backoffice",
                "com.zhonghe.adapter",
                "com.zhonghe.kernel"
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                LiquibaseAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class
        }
)
@EnableFeignClients(basePackages = "com.zhonghe.adapter.feign")
@Import({
        // 显式导入依赖模块的配置类（按需添加）
        com.zhonghe.backoffice.config.BackOfficeDataSourceConfig.class
})
public class AppBackofficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppBackofficeApplication.class, args);
    }
}