package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.StockTake;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockTakeMapper {
    // 插入主表数据(忽略重复)
    int insert(StockTake stockTake);

    // 批量插入主表数据(忽略重复)
    int batchInsert(List<StockTake> stockTakeList);
}
