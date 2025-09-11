package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.OperationLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper {
    void insert(OperationLog log);

    long selectOperationLogCount(Map<String, Object> params);

    List<OperationLog> selectOperationLogList(Map<String, Object> params);

}

