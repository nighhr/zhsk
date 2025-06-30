package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.ServiceCardEntry;

import java.util.List;

public interface ServiceCardEntryMapper {
    // 插入明细表数据
    int insertEntry(ServiceCardEntry entry);

    // 批量插入明细表数据
    int batchInsertEntry(List<ServiceCardEntry> entries);
}
