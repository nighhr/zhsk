package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.StoreTranEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreTranEntryMapper {
    int insertIgnore(StoreTranEntry entry);
    int batchInsertIgnore(@Param("list") List<StoreTranEntry> list);
}