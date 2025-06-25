package com.zhonghe.backoffice.service.Impl;

import com.zhonghe.backoffice.mapper.DbConnectionMapper;
import com.zhonghe.backoffice.model.DbConnection;
import com.zhonghe.backoffice.model.DTO.DbConnectionDTO;
import com.zhonghe.backoffice.service.DbConnectionService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class DbConnectionServiceImpl implements DbConnectionService {

    @Autowired
    private DbConnectionMapper dbConnectionMapper;

    /**
     * 分页查询数据库连接列表
     *
     * @param dto 查询参数DTO
     * @return 分页结果
     */
    public PageResult<DbConnection> getDbConnectionList(DbConnectionDTO dto) {
        // 转换为Map参数，便于MyBatis处理
        Map<String, Object> params = new HashMap<>();
        params.put("connectionName", dto.getConnectionName());
        params.put("dbName", dto.getDbName());
        params.put("connectionType", dto.getConnectionType());
        params.put("page", dto.getPage());
        params.put("pageSize", dto.getPageSize());

        // 计算offset
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        params.put("offset", offset);

        // 查询数据
        List<DbConnection> connections = dbConnectionMapper.selectDbConnectionList(params);
        long total = dbConnectionMapper.selectDbConnectionCount(params);

        // 构建分页结果
        PageResult<DbConnection> pageResult = new PageResult<>();
        pageResult.setList(connections);
        pageResult.setTotal(total);
        pageResult.setPage(dto.getPage());
        pageResult.setPageSize(dto.getPageSize());

        return pageResult;
    }

    /**
     * 创建数据库连接
     *
     * @param dbConnection 连接信息
     * @return 创建后的连接信息(包含ID)
     */
    public DbConnection createConnection(DbConnection dbConnection) {
        Date now = new Date();
        // 设置审计字段
        dbConnection.setCreateTime(now);
        dbConnection.setUpdateTime(now);
        dbConnection.setIsDeleted(false);

        // 密码加密处理
        if (StringUtils.hasText(dbConnection.getPassword())) {
            dbConnection.setPassword(encryptPassword(dbConnection.getPassword()));
        }

        dbConnectionMapper.insert(dbConnection);
        return dbConnection;
    }

    /**
     * 更新数据库连接
     *
     * @param dbConnection 连接信息
     * @return 更新后的连接信息
     */
    public DbConnection updateConnection(DbConnection dbConnection) {
        Date now = new Date();
        dbConnection.setUpdateTime(now);

        // 密码处理
        if (StringUtils.hasText(dbConnection.getPassword())) {
            dbConnection.setPassword(encryptPassword(dbConnection.getPassword()));
        } else {
            // 不更新密码
            dbConnection.setPassword(null);
        }

        dbConnectionMapper.updateByIdSelective(dbConnection);
        return dbConnectionMapper.selectById(dbConnection.getId());
    }

    /**
     * 删除数据库连接(逻辑删除)
     *
     * @param id 连接ID
     */
    public void deleteConnection(Long id) {
        DbConnection dbConnection = new DbConnection();
        dbConnection.setId(id);
        dbConnection.setIsDeleted(true);
        dbConnection.setUpdateTime(new Date());
        // 这里需要设置updater，您可以在Controller中获取当前用户后设置
        // dbConnection.setUpdater(currentUser);

        dbConnectionMapper.updateByIdSelective(dbConnection);
    }

    @Override
    public Result<String> testConnection(DbConnection dbConnection) {
        try {
            // 根据不同类型数据库进行测试
            switch (dbConnection.getConnectionType().toUpperCase()) {
                case "MYSQL":
                    return testMysqlConnection(dbConnection);
                case "ORACLE":
                    return testOracleConnection(dbConnection);
                case "SQLSERVER":
                    return testSqlServerConnection(dbConnection);
                case "POSTGRESQL":
                    return testPostgresqlConnection(dbConnection);
                default:
                    return Result.fail("不支持的数据库类型: " + dbConnection.getConnectionType());
            }
        } catch (Exception e) {
            return Result.fail("连接测试失败: " + e.getMessage());
        }
    }
    private Result testMysqlConnection(DbConnection dbConnection) {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&characterEncoding=%s",
                dbConnection.getDbHost(),
                dbConnection.getDbPort(),
                dbConnection.getDbName(),
                dbConnection.getCharset());

        return testJdbcConnection(
                "com.mysql.cj.jdbc.Driver",
                url,
                dbConnection.getUsername(),
                dbConnection.getPassword()
        );
    }

    private Result testOracleConnection(DbConnection dbConnection) {
        String url = String.format("jdbc:oracle:thin:@%s:%d:%s",
                dbConnection.getDbHost(),
                dbConnection.getDbPort(),
                dbConnection.getDbName());

        return testJdbcConnection(
                "oracle.jdbc.OracleDriver",
                url,
                dbConnection.getUsername(),
                dbConnection.getPassword()
        );
    }

    private Result testSqlServerConnection(DbConnection dbConnection) {
        String url = String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                dbConnection.getDbHost(),
                dbConnection.getDbPort(),
                dbConnection.getDbName());

        return testJdbcConnection(
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                url,
                dbConnection.getUsername(),
                dbConnection.getPassword()
        );
    }

    private Result testPostgresqlConnection(DbConnection dbConnection) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                dbConnection.getDbHost(),
                dbConnection.getDbPort(),
                dbConnection.getDbName());

        return testJdbcConnection(
                "org.postgresql.Driver",
                url,
                dbConnection.getUsername(),
                dbConnection.getPassword()
        );
    }

    private Result testJdbcConnection(String driverClass, String url, String username, String password) {
        Connection connection = null;
        try {
            // 加载驱动
            Class.forName(driverClass);

            // 设置连接超时为5秒
            DriverManager.setLoginTimeout(5);

            // 获取连接
            connection = DriverManager.getConnection(url, username, password);

            // 简单测试查询
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SELECT 1");
            }

            return Result.success("数据库连接测试成功");
        } catch (ClassNotFoundException e) {
            return Result.fail("找不到JDBC驱动: " + driverClass);
        } catch (SQLException e) {
            return Result.fail("数据库连接失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    /**
     * 密码加密方法
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String password) {
        // TODO: 实现密码加密逻辑，例如使用BCryptPasswordEncoder
        return password;
    }

}
