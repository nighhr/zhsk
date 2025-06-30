package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.SaleLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SaleLineMapper {
    void insertSaleLine(SaleLine saleEntry);
    void batchInsertSaleLine(List<SaleLine> saleEntries);
}
