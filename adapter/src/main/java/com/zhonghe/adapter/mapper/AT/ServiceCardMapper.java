package com.zhonghe.adapter.mapper.AT;

import com.zhonghe.adapter.model.ServiceCard;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ServiceCardMapper {
    // 插入主表数据(忽略重复)
    int insertIgnore(ServiceCard serviceCard);

    // 批量插入主表数据(忽略重复)
    int batchInsertIgnore(List<ServiceCard> list);

}