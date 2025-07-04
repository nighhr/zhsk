package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.DbConnection;
import java.util.List;
import java.util.Map;

public interface DbConnectionMapper {
    List<DbConnection> selectDbConnectionList(Map<String, Object> params);
    long selectDbConnectionCount(Map<String, Object> params);
    int insert(DbConnection dbConnection);
    int updateByIdSelective(DbConnection dbConnection);
    DbConnection selectById(Long id);
    List<DbConnection> selectAllAppNames();
}