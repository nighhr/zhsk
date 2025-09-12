package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.SaleRec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleRecMapper {
    int insert(SaleRec saleRec);
    int batchInsert(@Param("list")List<SaleRec> saleRecs);
    int batchUpdatePlatformArea(@Param("list")List<SaleRec> saleRecs);
    void deleteSaleRecByTime(@Param("start")String start, @Param("end")String end);
}
