package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockMapper {
    int insert(Stock stock);
    int update(Stock stock);
    int deleteById(@Param("id") Long id);
    Stock selectById(@Param("id") Long id);
    List<Stock> selectList(Map<String, Object> params);
    long count(Map<String, Object> params);
    int batchInsert(@Param("list") List<Stock> stocks);
    List<Stock> selectListByName(Map<String, Object> params);
    long selectCountByName(Map<String, Object> params);

    List<String> selectCode();
}
