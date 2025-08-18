package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.Sale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleMapper {
    int insert(Sale sale);
    int batchInsert(List<Sale> sales);

    void updateFSetType(@Param("start")String start, @Param("end")String end);
}
