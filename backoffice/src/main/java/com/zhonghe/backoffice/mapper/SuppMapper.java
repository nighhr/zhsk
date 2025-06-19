package com.zhonghe.backoffice.mapper;

import com.zhonghe.backoffice.model.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SuppMapper {

    int insert(Supplier supplier);

    int update(Supplier supplier);

    int deleteById(@Param("id") Long id);

    Supplier selectById(@Param("id") Long id);

    List<Supplier> selectList(Map<String, Object> params);

    long count(Map<String, Object> params);

    int batchInsert(@Param("list") List<Supplier> suppliers);
}