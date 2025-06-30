package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.StoreTran;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreTranMapper {
    int insertIgnore(StoreTran storeTran);
    int batchInsertIgnore(@Param("list") List<StoreTran> list);
}