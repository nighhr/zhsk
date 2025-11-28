package com.zhonghe.adapter.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zhonghe.adapter.mapper.AT.DynamicMapper;
import com.zhonghe.adapter.model.DbConnection;
import com.zhonghe.adapter.service.DbConnectionService;
import com.zhonghe.adapter.utils.PasswordUtils;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理器
 * 支持多数据源的创建、缓存、切换和恢复
 */
@Service
@Slf4j
public class DynamicDataSourceManager {

    @Autowired
    private DbConnectionService dbConnectionService;

    private final Map<String, DataSource> cachedDataSources = new ConcurrentHashMap<>();

    private final Map<String, SqlSessionFactory> cachedSqlSessionFactories = new ConcurrentHashMap<>();

    /**
     * ThreadLocal - 存储当前线程要使用的数据源key
     * 格式: "ds_" + connectionId
     * 为null表示使用默认数据源
     */
    private static final ThreadLocal<String> dataSourceContext = new ThreadLocal<>();

    /**
     * 根据数据源ID获取或创建DataSource
     */
    public DataSource getDataSource(Long connectionId) {
        String key = "ds_" + connectionId;

        if (cachedDataSources.containsKey(key)) {
            return cachedDataSources.get(key);
        }

        DbConnection connection = dbConnectionService.getConnectionById(connectionId);
        DataSource dataSource = createDataSource(connection);
        cachedDataSources.put(key, dataSource);

        log.info("创建新数据源: connectionId={}, poolName=DynamicPool_{}",
                connectionId, connection.getId());
        return dataSource;
    }

    /**
     * 根据数据源ID获取或创建SqlSessionFactory
     */
    public SqlSessionFactory getSqlSessionFactory(Long connectionId) {
        String key = "ssf_" + connectionId;

        if (cachedSqlSessionFactories.containsKey(key)) {
            return cachedSqlSessionFactories.get(key);
        }

        DataSource dataSource = getDataSource(connectionId);
        SqlSessionFactory factory = createSqlSessionFactory(dataSource, connectionId);
        cachedSqlSessionFactories.put(key, factory);

        log.info("创建新SqlSessionFactory: connectionId={}", connectionId);
        return factory;
    }

    /**
     * 使用指定数据源执行SQL查询
     */
    public List<Map<String, Object>> executeSql(Long connectionId, String sql) {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory(connectionId);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            DynamicMapper mapper = sqlSession.getMapper(DynamicMapper.class);
            log.info("执行查询SQL - connectionId: {}, sql: {}", connectionId, sql);

            List<Map<String, Object>> result = mapper.selectBySql(sql);
            log.info("查询结果条数: {}", result.size());

            return result;
        } catch (Exception e) {
            log.error("SQL执行失败 - connectionId: {}", connectionId, e);
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,
                    "SQL执行失败: " + e.getMessage());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用指定数据源执行SQL查询 - 返回单条记录
     */
    public Map<String, Object> executeSqlOne(Long connectionId, String sql) {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory(connectionId);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            DynamicMapper mapper = sqlSession.getMapper(DynamicMapper.class);
            log.info("执行单条查询SQL - connectionId: {}", connectionId);

            return mapper.selectOneBySql(sql);
        } catch (Exception e) {
            log.error("SQL执行失败 - connectionId: {}", connectionId, e);
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,
                    "SQL执行失败: " + e.getMessage());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * ========== 核心方法：切换数据源 ==========
     * 将指定的数据源key保存到ThreadLocal中
     * 后续的JdbcTemplate、MyBatis等操作会使用这个数据源
     */
    public void switchDataSource(Long connectionId) {
        if (connectionId == null || connectionId == 0) {
            log.debug("connectionId为空或0，不切换数据源");
            return;
        }

        String key = "ds_" + connectionId;

        // 确保数据源存在（如果不存在则创建）
        if (!cachedDataSources.containsKey(key)) {
            getDataSource(connectionId);
        }

        // ========== 关键：将数据源key保存到ThreadLocal ==========
        dataSourceContext.set(key);
        log.info("已切换数据源: connectionId={}, key={}", connectionId, key);
    }

    /**
     * ========== 核心方法：恢复默认数据源 ==========
     * 清空ThreadLocal中的值，恢复为默认数据源
     */
    public void restoreDataSource() {
        dataSourceContext.remove();
        log.debug("已清除数据源上下文，恢复为默认数据源");
    }

    /**
     * ========== 核心方法：获取当前线程的数据源key ==========
     * 调用处（JdbcTemplate、MyBatis等）可以通过这个方法获取当前要使用的数据源
     * 如果返回null，则使用默认数据源
     */
    public String getCurrentDataSourceKey() {
        return dataSourceContext.get();
    }

    /**
     * ========== 核心方法：获取当前线程应该使用的数据源 ==========
     * 这个方法是关键！它根据ThreadLocal中的值返回对应的DataSource
     * 在JdbcTemplate或其他地方使用时，调用这个方法而不是直接使用某个固定的DataSource
     */
    public DataSource getCurrentDataSource() {
        String key = getCurrentDataSourceKey();

        if (key == null) {
            // 没有设置特定的数据源，返回null（让Spring使用默认数据源）
            log.debug("使用默认数据源");
            return null;
        }

        DataSource dataSource = cachedDataSources.get(key);
        if (dataSource == null) {
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,
                    "数据源不存在: " + key);
        }

        log.debug("使用动态数据源: {}", key);
        return dataSource;
    }

    /**
     * 创建数据源
     */
    private DataSource createDataSource(DbConnection connection) {
        HikariDataSource dataSource = new HikariDataSource();

        String url = buildJdbcUrl(connection);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(connection.getUsername());
        dataSource.setPassword(decryptPassword(connection.getPassword()));
        dataSource.setDriverClassName(getDriverClassName(connection.getConnectionType()));

        // Hikari连接池配置
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setPoolName("DynamicPool_" + connection.getId());

        return dataSource;
    }

    /**
     * 根据连接类型构建JDBC URL
     */
    private String buildJdbcUrl(DbConnection connection) {
        String type = connection.getConnectionType().toUpperCase();

        switch (type) {
            case "MYSQL":
                return String.format(
                        "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=%s&allowPublicKeyRetrieval=true",
                        connection.getDbHost(),
                        connection.getDbPort(),
                        connection.getDbName(),
                        connection.getCharset() != null ? connection.getCharset() : "utf8mb4"
                );

            case "SQLSERVER":
                return String.format(
                        "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true;serverTimezone=Asia/Shanghai",
                        connection.getDbHost(),
                        connection.getDbPort(),
                        connection.getDbName()
                );

            case "ORACLE":
                return String.format(
                        "jdbc:oracle:thin:@//%s:%d/%s",
                        connection.getDbHost(),
                        connection.getDbPort(),
                        connection.getDbName()
                );

            case "POSTGRESQL":
                return String.format(
                        "jdbc:postgresql://%s:%d/%s?serverTimezone=Asia/Shanghai&characterEncoding=%s",
                        connection.getDbHost(),
                        connection.getDbPort(),
                        connection.getDbName(),
                        connection.getCharset() != null ? connection.getCharset() : "UTF-8"
                );

            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR,
                        "不支持的数据库类型: " + type);
        }
    }

    /**
     * 根据连接类型获取驱动类名
     */
    private String getDriverClassName(String connectionType) {
        String type = connectionType.toUpperCase();

        switch (type) {
            case "MYSQL":
                return "com.mysql.cj.jdbc.Driver";
            case "SQLSERVER":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "ORACLE":
                return "oracle.jdbc.OracleDriver";
            case "POSTGRESQL":
                return "org.postgresql.Driver";
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR,
                        "不支持的数据库类型: " + type);
        }
    }

    /**
     * 创建SqlSessionFactory
     */
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Long connectionId) {
        try {
            SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
            factoryBean.setDataSource(dataSource);

            org.apache.ibatis.session.Configuration config =
                    new org.apache.ibatis.session.Configuration();
            config.setMapUnderscoreToCamelCase(true);
            config.setUseGeneratedKeys(true);
            config.setDefaultExecutorType(ExecutorType.BATCH);
            factoryBean.setConfiguration(config);

            return factoryBean.getObject();
        } catch (Exception e) {
            log.error("创建SqlSessionFactory失败: connectionId={}", connectionId, e);
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,
                    "创建SqlSessionFactory失败: " + e.getMessage());
        }
    }

    /**
     * 解密密码
     */
    private String decryptPassword(String encryptedPassword) {
        try {
            return PasswordUtils.decryptDBPassword(encryptedPassword);
        } catch (Exception e) {
            log.error("密码解密失败", e);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码解密失败");
        }
    }

    /**
     * 关闭指定数据源
     */
    public void closeDataSource(Long connectionId) {
        String key = "ds_" + connectionId;
        DataSource dataSource = cachedDataSources.remove(key);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            log.info("数据源已关闭: connectionId={}", connectionId);
        }
        cachedSqlSessionFactories.remove("ssf_" + connectionId);
    }

    /**
     * 清空所有缓存的数据源
     */
    public void clearCache() {
        cachedDataSources.values().forEach(ds -> {
            if (ds instanceof HikariDataSource) {
                ((HikariDataSource) ds).close();
            }
        });
        cachedDataSources.clear();
        cachedSqlSessionFactories.clear();
        log.info("所有数据源缓存已清空");
    }
}