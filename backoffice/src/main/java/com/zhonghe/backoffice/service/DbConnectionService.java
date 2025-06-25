package com.zhonghe.backoffice.service;


import com.zhonghe.backoffice.model.DTO.DbConnectionDTO;
import com.zhonghe.backoffice.model.DbConnection;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;

public interface DbConnectionService {
    PageResult<DbConnection> getDbConnectionList(DbConnectionDTO dto);

    DbConnection createConnection(DbConnection dbConnection);

    DbConnection updateConnection(DbConnection dbConnection);

    void deleteConnection(Long id);

    Result<String> testConnection(DbConnection dbConnection);
}
