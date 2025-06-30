package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.PurRet;
import com.zhonghe.adapter.model.PurRetLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PurRetMapper {
    // 主表插入
    int insert(PurRet purRet);

    // 主表批量插入
    int batchInsert(List<PurRet> purRetList);


}