package com.zhonghe.adapter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 动态 JdbcTemplate 配置
 * 使 JdbcTemplate 能够根据 ThreadLocal 动态切换数据源
 */
@Configuration
@Slf4j
public class DynamicJdbcConfig {

    @Autowired(required = false)
    private DynamicDataSourceManager dynamicDataSourceManager;

    /**
     * 从 YML 读取默认数据源
     * 注意：如果 YML 中没有 spring.datasource.primary，会从 spring.datasource 读取
     */
    @Bean(name = "primaryDefaultDataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource primaryDefaultDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * ========== 动态路由数据源实现 ==========
     * 类似 SecondaryDataSourceConfig 中的 RoutingDataSource
     */
    @Slf4j
    public static class DynamicRoutingDataSource extends AbstractRoutingDataSource {

        private DynamicDataSourceManager dynamicDataSourceManager;

        public void setDynamicDataSourceManager(DynamicDataSourceManager manager) {
            this.dynamicDataSourceManager = manager;
        }

        /**
         * 决定当前使用哪个数据源的 key
         */
        @Override
        protected Object determineCurrentLookupKey() {
            if (dynamicDataSourceManager != null) {
                String currentKey = dynamicDataSourceManager.getCurrentDataSourceKey();
                if (currentKey != null) {
                    log.debug("JdbcTemplate 使用动态数据源: {}", currentKey);
                    return currentKey;
                }
            }
            log.debug("JdbcTemplate 使用默认数据源");
            return "primary";
        }

        /**
         * 重写 getConnection() - 这是关键！
         */
        @Override
        public Connection getConnection() throws java.sql.SQLException {
            try {
                return super.getConnection();
            } catch (Exception e) {
                log.debug("标准流程失败，尝试从动态数据源获取");

                if (dynamicDataSourceManager != null) {
                    DataSource dynamicDataSource = dynamicDataSourceManager.getCurrentDataSource();
                    if (dynamicDataSource != null) {
                        return dynamicDataSource.getConnection();
                    }
                }

                // 降级到默认数据源
                return getDefaultDataSource().getConnection();
            }
        }

        /**
         * 重写 getTargetDataSource() - 这也是关键！
         */
        protected DataSource getTargetDataSource() {
            Object lookupKey = determineCurrentLookupKey();

            if (lookupKey != null && !"primary".equals(lookupKey) && dynamicDataSourceManager != null) {
                DataSource dynamicDataSource = dynamicDataSourceManager.getCurrentDataSource();
                if (dynamicDataSource != null) {
                    log.debug("返回动态数据源: {}", lookupKey);
                    return dynamicDataSource;
                }
            }

            log.debug("返回默认数据源");
            return getDefaultDataSource();
        }

        private DataSource getDefaultDataSource() {
            return (DataSource) getResolvedDefaultDataSource();
        }
    }
}