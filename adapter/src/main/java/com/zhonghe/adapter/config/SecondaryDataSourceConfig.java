package com.zhonghe.adapter.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 改造后的 SecondaryDataSourceConfig - U8 Mapper 数据源配置
 * 支持动态数据源切换
 */
@Configuration
@MapperScan(
        basePackages = "com.zhonghe.adapter.mapper.U8",
        sqlSessionFactoryRef = "secondarySqlSessionFactory"
)
@Slf4j
public class SecondaryDataSourceConfig {

    @Autowired(required = false)
    private DynamicDataSourceManager dynamicDataSourceManager;

    /**
     * 从配置文件读取的默认Secondary数据源（U8数据库）
     */
    @Bean(name = "secondaryDefaultDataSource")
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSource secondaryDefaultDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * 创建动态路由数据源
     */
    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource(@Qualifier("secondaryDefaultDataSource") DataSource defaultDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource(dynamicDataSourceManager, defaultDataSource);

        // 设置默认数据源
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        // 设置空的目标数据源Map（主要通过 determineCurrentLookupKey 动态获取）
        Map<Object, Object> targetDataSources = new HashMap<>();
        routingDataSource.setTargetDataSources(targetDataSources);

        return routingDataSource;
    }

    @Bean
    public SqlSessionFactory secondarySqlSessionFactory(
            @Qualifier("secondaryDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/U8/*.xml"));
        factoryBean.setTypeAliasesPackage("com.zhonghe.adapter.model.U8");

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(config);

        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate secondarySqlSessionTemplate(
            @Qualifier("secondarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * ========== 核心：动态路由DataSource ==========
     * 根据 DynamicDataSourceManager 中的 ThreadLocal 动态返回数据源
     */
    @Slf4j
    public static class RoutingDataSource extends AbstractRoutingDataSource {

        private DynamicDataSourceManager dynamicDataSourceManager;
        private DataSource defaultDataSource;

        public RoutingDataSource(DynamicDataSourceManager dynamicDataSourceManager, DataSource defaultDataSource) {
            this.dynamicDataSourceManager = dynamicDataSourceManager;
            this.defaultDataSource = defaultDataSource;
        }

        /**
         * 决定当前应该使用哪个数据源
         * 返回值会被传给 getTargetDataSource() 来查找对应的 DataSource
         */
        @Override
        protected Object determineCurrentLookupKey() {
            // 如果有动态数据源管理器，尝试从中获取当前数据源key
            if (dynamicDataSourceManager != null) {
                String currentKey = dynamicDataSourceManager.getCurrentDataSourceKey();
                if (currentKey != null) {
                    log.debug("使用动态数据源: {}", currentKey);
                    return currentKey;
                }
            }

            // 没有设置动态数据源，返回 null 表示使用默认数据源
            log.debug("使用默认数据源");
            return null;
        }

        /**
         * 重写这个方法来处理动态数据源的获取
         * 当 determineCurrentLookupKey() 返回的key在 targetDataSources 中找不到时调用
         */
        @Override
        public Connection getConnection() throws java.sql.SQLException {
            // 首先尝试按照标准流程获取
            try {
                return super.getConnection();
            } catch (Exception e) {
                log.debug("标准流程获取失败，尝试从动态数据源管理器获取");

                // 如果标准流程失败，尝试从动态数据源管理器获取
                if (dynamicDataSourceManager != null) {
                    DataSource currentDataSource = dynamicDataSourceManager.getCurrentDataSource();
                    if (currentDataSource != null) {
                        return currentDataSource.getConnection();
                    }
                }

                // 最后使用默认数据源
                return defaultDataSource.getConnection();
            }
        }

        /**
         * 重写 getTargetDataSource() 来支持动态获取
         * 这是关键方法！
         */
        protected DataSource getTargetDataSource() {
            Object lookupKey = determineCurrentLookupKey();

            // 如果lookupKey不为null，说明要使用动态数据源
            if (lookupKey != null && dynamicDataSourceManager != null) {
                DataSource dynamicDataSource = dynamicDataSourceManager.getCurrentDataSource();
                if (dynamicDataSource != null) {
                    log.debug("返回动态数据源");
                    return dynamicDataSource;
                }
            }

            // 使用默认数据源
            log.debug("返回默认数据源");
            return defaultDataSource;
        }
    }
}