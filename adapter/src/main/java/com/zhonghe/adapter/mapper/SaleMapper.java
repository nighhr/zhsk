package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.Sale;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SaleMapper {
    int insert(Sale sale);
    int batchInsert(List<Sale> sales);
}
