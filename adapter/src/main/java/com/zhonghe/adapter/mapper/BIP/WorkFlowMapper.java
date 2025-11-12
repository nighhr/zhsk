package com.zhonghe.adapter.mapper.BIP;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface WorkFlowMapper {

    List<HashMap<String, Object>> selectCheckManAndMessageNote(@Param("billtype") String billtype);
}
