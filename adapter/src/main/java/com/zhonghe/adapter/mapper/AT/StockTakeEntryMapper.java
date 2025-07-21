package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.StockTakeEntry;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockTakeEntryMapper {
    // 插入明细数据(忽略重复)
    int insertEntry(StockTakeEntry entry);

    // 批量插入明细数据(忽略重复)
    int batchInsertEntry(List<StockTakeEntry> entryList);
}
