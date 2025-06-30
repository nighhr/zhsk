package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.PurRetLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PurRetLineMapper {

    int insertLine(PurRetLine purRetLine);

    // 明细表批量插入
    int batchInsertLines(List<PurRetLine> purRetLines);

}
