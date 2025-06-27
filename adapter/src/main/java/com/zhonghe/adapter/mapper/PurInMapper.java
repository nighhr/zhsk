package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.PurIn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PurInMapper {
    int insert(PurIn purIn);
    int batchInsert(List<PurIn> purIns);
}