package com.zhonghe.backoffice.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.zhonghe.backoffice.mapper", sqlSessionFactoryRef = "backofficeSqlSessionFactory")
public class BackOfficeDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.backoffice")
    public DataSource backofficeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory backofficeSqlSessionFactory(
            @Qualifier("backofficeDataSource") DataSource ds) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(ds);
        // 注意：根据你项目的资源路径设置 mapper 位置
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath*:mapper/*.xml"));
        bean.setTypeAliasesPackage("com.zhonghe.backoffice.model");
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(config);
        return bean.getObject();
    }

    @Bean
    public DataSourceTransactionManager backofficeTxManager(
            @Qualifier("backofficeDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public SqlSessionTemplate backofficeSqlSessionTemplate(
            @Qualifier("backofficeSqlSessionFactory") SqlSessionFactory factory) {
        return new SqlSessionTemplate(factory);
    }
}
