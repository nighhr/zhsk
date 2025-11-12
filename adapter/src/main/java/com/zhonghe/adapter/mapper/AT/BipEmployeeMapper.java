package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.BIP.BipEmployee;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BipEmployeeMapper {

    int batchInsert(@Param("list") List<BipEmployee> list);

    int updateDingdingId(@Param("code") String code, @Param("dingdingId") String dingdingId);

    @MapKey("pkPsndoc")
    Map<String, String> selectDingDingIdByPsnDoc(@Param("pkPsndocList") List<String> pkPsndocList);
}
