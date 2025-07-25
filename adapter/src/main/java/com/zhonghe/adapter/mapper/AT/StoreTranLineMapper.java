package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.StoreTranLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreTranLineMapper {
    int batchInsert(@Param("list") List<StoreTranLine> list);
}