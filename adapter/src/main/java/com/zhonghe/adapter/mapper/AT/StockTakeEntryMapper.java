package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.StockTakeLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockTakeEntryMapper {
    // 批量插入明细数据(忽略重复)
    int batchInsert(List<StockTakeLine> entryList);
}
