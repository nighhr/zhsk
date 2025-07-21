package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.DbConnection;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface DbConnectionMapper {
    List<DbConnection> selectDbConnectionList(Map<String, Object> params);
    long selectDbConnectionCount(Map<String, Object> params);
    int insert(DbConnection dbConnection);
    int updateByIdSelective(DbConnection dbConnection);
    DbConnection selectById(Long id);
    List<DbConnection> selectAllAppNames();

    List<String> getFieldsByTableName(String tableName);
}