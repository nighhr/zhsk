package com.zhonghe.backoffice.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zhonghe.adapter.config.DynamicDataSourceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Primary
@Configuration
@MapperScan(
        basePackages = {"com.zhonghe.backoffice.mapper", "com.zhonghe.adapter.mapper.AT"},
        sqlSessionFactoryRef = "backofficeSqlSessionFactory"
)
@Slf4j
public class BackOfficeDataSourceConfig {


    @Autowired(required = false)
    private ObjectProvider<DynamicDataSourceManager> dynamicDataSourceManagerProvider;


    @Bean
    @ConfigurationProperties("spring.datasource.backoffice")
    public DataSource backofficeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "dynamicRoutingDataSource")
    public DataSource dynamicRoutingDataSource(
            @Qualifier("backofficeDataSource") DataSource defaultDataSource) {
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();

        // 延迟获取，避免循环依赖
        routingDataSource.setDynamicDataSourceManager(
                dynamicDataSourceManagerProvider.getIfAvailable()
        );

        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        Map<Object, Object> targetDataSources = new HashMap<>();
        routingDataSource.setTargetDataSources(targetDataSources);

        return routingDataSource;
    }

    @Bean
    public SqlSessionFactory backofficeSqlSessionFactory(
            @Qualifier("backofficeDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml"));
        bean.setTypeAliasesPackage("com.zhonghe.backoffice.model,com.zhonghe.adapter.model.AT");

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(config);

        return bean.getObject();
    }

    @Bean
    public SqlSessionTemplate backofficeSqlSessionTemplate(
            @Qualifier("backofficeSqlSessionFactory") SqlSessionFactory factory) {
        return new SqlSessionTemplate(factory);
    }

    @Bean
    public DataSourceTransactionManager backofficeTxManager(
            @Qualifier("backofficeDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    /**
     * ========== 关键：替换为动态 JdbcTemplate ==========
     */
    @Bean(name = "dynamicJdbcTemplate")
    public JdbcTemplate dynamicJdbcTemplate(
            @Qualifier("dynamicRoutingDataSource") DataSource dynamicRoutingDataSource) {
        return new JdbcTemplate(dynamicRoutingDataSource);
    }

    /**
     * 保留原有的 backofficeJdbcTemplate（向后兼容）
     */
    @Bean(name = "backofficeJdbcTemplate")
    public JdbcTemplate backofficeJdbcTemplate(
            @Qualifier("backofficeDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * ========== 动态路由 DataSource 内部类 ==========
     */
    @Slf4j
    public static class DynamicRoutingDataSource extends AbstractRoutingDataSource {

        private com.zhonghe.adapter.config.DynamicDataSourceManager dynamicDataSourceManager;

        public void setDynamicDataSourceManager(
                com.zhonghe.adapter.config.DynamicDataSourceManager manager) {
            this.dynamicDataSourceManager = manager;
        }

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
            return "default";
        }

        @Override
        public Connection getConnection() throws java.sql.SQLException {
            try {
                return super.getConnection();
            } catch (Exception e) {
                log.debug("标准流程失败，尝试动态获取");
                if (dynamicDataSourceManager != null) {
                    DataSource dynamicDataSource = dynamicDataSourceManager.getCurrentDataSource();
                    if (dynamicDataSource != null) {
                        return dynamicDataSource.getConnection();
                    }
                }
                return getDefaultDataSource().getConnection();
            }
        }

        protected DataSource getTargetDataSource() {
            Object lookupKey = determineCurrentLookupKey();
            if (lookupKey != null && !"default".equals(lookupKey) && dynamicDataSourceManager != null) {
                DataSource dynamicDataSource = dynamicDataSourceManager.getCurrentDataSource();
                if (dynamicDataSource != null) {
                    log.debug("返回动态数据源");
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