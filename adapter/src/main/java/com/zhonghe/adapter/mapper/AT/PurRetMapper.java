package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.PurRet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PurRetMapper {
    // 主表插入
    int insert(PurRet purRet);

}