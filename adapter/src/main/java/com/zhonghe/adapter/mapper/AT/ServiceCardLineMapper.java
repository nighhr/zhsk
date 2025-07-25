package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.ServiceCardLine;

import java.util.List;

public interface ServiceCardLineMapper {
    // 批量插入明细表数据
    int batchInsert(List<ServiceCardLine> entries);
}
