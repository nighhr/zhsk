package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.PurRetLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PurRetLineMapper {

    // 明细表批量插入
    int batchInsertLines(List<PurRetLine> purRetLines);

}
