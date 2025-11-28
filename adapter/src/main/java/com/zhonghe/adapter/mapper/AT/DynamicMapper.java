package com.zhonghe.adapter.mapper.AT;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DynamicMapper {

    /**
     * 执行原生SQL查询 - 返回Map列表
     */
    @Select("${sql}")
    List<Map<String, Object>> selectBySql(@Param("sql") String sql);

    /**
     * 执行原生SQL查询 - 返回单条记录
     */
    @Select("${sql}")
    Map<String, Object> selectOneBySql(@Param("sql") String sql);
}