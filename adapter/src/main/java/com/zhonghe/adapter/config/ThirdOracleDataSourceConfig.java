package com.zhonghe.adapter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Configuration
@MapperScan(
        basePackages = "com.zhonghe.adapter.mapper.BIP",
        sqlSessionFactoryRef = "thirdSqlSessionFactory"
)
@Slf4j
public class ThirdOracleDataSourceConfig {

    @Bean(name = "thirdDefaultDataSource")
    @ConfigurationProperties("spring.datasource.third")
    public DataSource thirdDefaultDataSource() {
        try {
            return DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .build();
        } catch (Exception e) {
            log.warn("从 YML 读取 third 数据源失败，返回虚拟数据源: {}", e.getMessage());
            // 如果 YML 中没有 third 配置，返回虚拟数据源
            return new org.springframework.jdbc.datasource.DriverManagerDataSource();
        }
    }

    @Bean(name = "thirdDataSource")
    public DataSource thirdDataSource(
            @Autowired(required = false) DynamicDataSourceManager dynamicDataSourceManager) {

        ThirdRoutingDataSource routingDataSource = new ThirdRoutingDataSource();
        routingDataSource.setDynamicDataSourceManager(dynamicDataSourceManager);

        // 设置默认数据源（从 YML 读取）
        try {
            DataSource defaultDataSource = thirdDefaultDataSource();
            routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        } catch (Exception e) {
            log.warn("设置默认 third 数据源失败: {}", e.getMessage());
        }

        Map<Object, Object> targetDataSources = new HashMap<>();
        routingDataSource.setTargetDataSources(targetDataSources);

        return routingDataSource;
    }

    @Bean(name = "thirdSqlSessionFactory")
    public SqlSessionFactory thirdSqlSessionFactory(
            @org.springframework.beans.factory.annotation.Qualifier("thirdDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/BIP/*.xml"));
        factoryBean.setTypeAliasesPackage("com.zhonghe.adapter.model.BIP");

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(config);

        return factoryBean.getObject();
    }

    @Bean(name = "thirdSqlSessionTemplate")
    public SqlSessionTemplate thirdSqlSessionTemplate(
            @org.springframework.beans.factory.annotation.Qualifier("thirdSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Slf4j
    public static class ThirdRoutingDataSource extends AbstractRoutingDataSource {

        private DynamicDataSourceManager dynamicDataSourceManager;

        public void setDynamicDataSourceManager(DynamicDataSourceManager manager) {
            this.dynamicDataSourceManager = manager;
        }

        @Override
        protected Object determineCurrentLookupKey() {
            if (dynamicDataSourceManager != null) {
                String currentKey = dynamicDataSourceManager.getCurrentDataSourceKey();
                if (currentKey != null) {
                    log.debug("BIP Mapper 使用动态数据源: {}", currentKey);
                    return currentKey;
                }
            }
            log.debug("BIP Mapper 使用默认数据源");
            return null;
        }

        @Override
        public Connection getConnection() throws java.sql.SQLException {
            if (dynamicDataSourceManager != null) {
                DataSource currentDataSource = dynamicDataSourceManager.getCurrentDataSource();
                if (currentDataSource != null) {
                    log.debug("从 DynamicDataSourceManager 获取 BIP 数据源连接");
                    return currentDataSource.getConnection();
                }
            }

            // 使用默认数据源
            log.debug("使用默认 BIP 数据源连接");
            return getDefaultDataSource().getConnection();
        }

        protected DataSource getTargetDataSource() {
            if (dynamicDataSourceManager != null) {
                DataSource dynamicDataSource = dynamicDataSourceManager.getCurrentDataSource();
                if (dynamicDataSource != null) {
                    log.debug("返回 BIP Mapper 动态数据源");
                    return dynamicDataSource;
                }
            }

            log.debug("返回 BIP 默认数据源");
            return getDefaultDataSource();
        }

        private DataSource getDefaultDataSource() {
            return (DataSource) getResolvedDefaultDataSource();
        }
    }
}