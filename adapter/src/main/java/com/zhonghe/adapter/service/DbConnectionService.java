package com.zhonghe.adapter.service;


import com.zhonghe.adapter.model.DbConnection;
import com.zhonghe.adapter.model.DbConnectionDTO;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;

import java.util.List;

public interface DbConnectionService {
    PageResult<DbConnection> getDbConnectionList(DbConnectionDTO dto);

    DbConnection createConnection(DbConnection dbConnection) throws Exception;

    DbConnection updateConnection(DbConnection dbConnection) throws Exception;

    void deleteConnection(Long id);

    Result<String> testConnection(DbConnection dbConnection);

    List<DbConnection> getSimpleList();

    List<String> getTablesByConnectionId(Long connectionId);
    /**
     * 根据数据库名获取所有表名
     * @param databaseId 数据库id
     * @return 表名列表
     * @throws Exception 如果获取过程中出现错误
     */
    List<String> getAllTableNames(Long databaseId) throws Exception;

    List<String> getTableFields(String[] tableNames);

    DbConnection getConnectionById(Long connectionId);
}
