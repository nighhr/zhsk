package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.InsertionErrorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InsertionErrorLogMapper {
    void batchInsertErrors(@Param("list") List<InsertionErrorLog> errorLogs);
}
