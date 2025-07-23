package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.ServiceBox;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ServiceBoxMapper {
    int insert(ServiceBox serviceBox);

    void batchInsert(List<ServiceBox> serviceBoxList);
}
