package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
        com.zhonghe.adapter.config.SecondaryDataSourceConfig.class,
        com.zhonghe.backoffice.config.BackOfficeDataSourceConfig.class
})
@EnableAsync
@EnableTransactionManagement
public class AppBackofficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppBackofficeApplication.class, args);
    }
}