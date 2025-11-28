package com.zhonghe.adapter.service.Impl;

import com.zhonghe.adapter.mapper.AT.DbConnectionMapper;
import com.zhonghe.adapter.model.DbConnection;
import com.zhonghe.adapter.model.DbConnectionDTO;
import com.zhonghe.adapter.utils.PasswordUtils;
import com.zhonghe.adapter.service.DbConnectionService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static com.zhonghe.adapter.utils.PasswordUtils.*;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
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
    public DbConnection createConnection(DbConnection dbConnection) throws Exception {
        Date now = new Date();
        // 设置审计字段
        dbConnection.setCreateTime(now);
        dbConnection.setUpdateTime(now);
        dbConnection.setIsDeleted(false);

        // 密码加密处理
        if (StringUtils.hasText(dbConnection.getPassword())) {
            dbConnection.setPassword(PasswordUtils.encryptDBPassword(dbConnection.getPassword()));
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
    public DbConnection updateConnection(DbConnection dbConnection) throws Exception {
        Date now = new Date();
        dbConnection.setUpdateTime(now);

        // 密码处理
        if (!isEncryptedPassword(dbConnection.getPassword())) {
            dbConnection.setPassword(PasswordUtils.encryptDBPassword(dbConnection.getPassword()));
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

        dbConnectionMapper.updateByIdSelective(dbConnection);
    }

    @Override
    public Result<String> testConnection(DbConnection dbConnection) {
        String safePassword = getSafePassword(dbConnection.getPassword());
        dbConnection.setPassword(safePassword);
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

    @Override
    public List<DbConnection> getSimpleList() {
        return dbConnectionMapper.selectAllAppNames();
    }



    @Override
    public List<String> getTablesByConnectionId(Long connectionId) {
        DbConnection connection = dbConnectionMapper.selectById(connectionId);
        if (connection == null) {
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,"数据库连接不存在");
        }

        // 根据连接信息获取数据库中的所有表
        try {
            return getDatabaseTables(connection);
        } catch (SQLException e) {
            throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,"获取数据库表失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAllTableNames(Long databaseId) throws Exception {
        List<String> tableNames = new ArrayList<>();
        Connection connection = null;
        ResultSet resultSet = null;

        // 根据ID获取数据库连接信息
        DbConnection dbConnection = dbConnectionMapper.selectById(databaseId);
        if (dbConnection == null) {
            throw new IllegalArgumentException("数据库连接配置不存在，ID: " + databaseId);
        }

        try {
            // 1. 构建正确的JDBC URL
            String jdbcUrl = buildJdbcUrl(dbConnection);

            // 2. 获取数据库连接
            connection = DriverManager.getConnection(
                    jdbcUrl,
                    dbConnection.getUsername(),
                    decryptDBPassword(dbConnection.getPassword())
            );

            // 3. 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();

            // 4. 获取指定数据库的所有表
            resultSet = metaData.getTables(dbConnection.getDbName(), null, null, new String[]{"TABLE"});

            // 5. 遍历结果集获取表名
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
            }

            return tableNames;

        } finally {
            // 6. 关闭资源
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("关闭ResultSet失败", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("关闭Connection失败", e);
                }
            }
        }
    }

    @Override
    public List<String> getTableFields(String[] tableNames) {
        Set<String> fieldSet = new HashSet<>();

        // 查询每个表的字段信息，排除ID字段并去重
        for (String tableName : tableNames) {
            List<String> fields = dbConnectionMapper.getFieldsByTableName(tableName).stream()
                    .filter(field -> !"id".equalsIgnoreCase(field))
                    .collect(Collectors.toList());
            fieldSet.addAll(fields);
        }

        return new ArrayList<>(fieldSet);
    }

    @Override
    public DbConnection getConnectionById(Long connectionId) {
        return dbConnectionMapper.selectById(connectionId);
    }


    private List<String> getDatabaseTables(DbConnection connection) throws SQLException {
        String url = buildJdbcUrl(connection);
        String username = connection.getUsername();
        // TODO 加密解密需要添加
//        String password = encryptPassword(connection.getPassword()); // 解密密码
//        try (Connection conn = DriverManager.getConnection(url, username, password)) {

        try (Connection conn = DriverManager.getConnection(url, username, connection.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(connection.getDbName(), null, "%", new String[]{"TABLE"});

            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }
            return tableNames;
        }
    }
    private String buildJdbcUrl(DbConnection connection) {
        String dbType = connection.getConnectionType().toLowerCase();
        String host = connection.getDbHost();
        int port = connection.getDbPort();
        String dbName = connection.getDbName();
        String charset = connection.getCharset();

        switch (dbType) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=%s&serverTimezone=Asia/Shanghai",
                        host, port, dbName, charset);

            case "oracle":
                return String.format("jdbc:oracle:thin:@//%s:%d/%s",
                        host, port, dbName);

            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s",
                        host, port, dbName);

            case "sqlserver":
                // SQL Server 字符集处理
                String sqlServerCharsetParams = buildSqlServerCharsetParams(charset);
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true%s",
                        host, port, dbName, sqlServerCharsetParams);


            default:
                throw new BusinessException(ErrorCode.DB_CONNECT_ERROR,"不支持的数据库类型: " + dbType);
        }
    }

    /**
     * 构建 SQL Server 字符集相关参数
     */
    private String buildSqlServerCharsetParams(String charset) {
        StringBuilder params = new StringBuilder();

        // 常见字符集映射
        if ("UTF-8".equalsIgnoreCase(charset)) {
            params.append(";sendStringParametersAsUnicode=true");
            params.append(";useUnicode=true");
        } else if ("GBK".equalsIgnoreCase(charset) || "GB2312".equalsIgnoreCase(charset)) {
            params.append(";sendStringParametersAsUnicode=false");
            params.append(";useUnicode=false");
        } else {
            // 默认处理
            params.append(";sendStringParametersAsUnicode=true");
        }

        // 添加字符集参数
        params.append(";characterEncoding=").append(charset);

        return params.toString();
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
