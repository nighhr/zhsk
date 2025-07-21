package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.PurInLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PurInLineMapper {

        int insert(PurInLine purInLine);
        int batchInsert(List<PurInLine> purInLines);
}
