package com.zhonghe.backoffice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Primary
@Configuration
@MapperScan(
        basePackages = {"com.zhonghe.backoffice.mapper","com.zhonghe.adapter.mapper.AT"},
        sqlSessionFactoryRef = "backofficeSqlSessionFactory"
)
public class BackOfficeDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.backoffice")
    public DataSource backofficeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
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

    @Bean
    public JdbcTemplate backofficeJdbcTemplate(
            @Qualifier("backofficeDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
