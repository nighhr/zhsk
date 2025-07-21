package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.SaleRec;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SaleRecMapper {
    int insert(SaleRec saleRec);
    int batchInsert(List<SaleRec> saleRecs);
}
