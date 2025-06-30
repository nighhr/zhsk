package com.zhonghe.adapter.mapper;

import com.zhonghe.adapter.model.ServiceCost;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ServiceCostMapper {
    /**
     * 插入单条记录(忽略重复)
     * @param serviceCost 服务消费记录
     * @return 影响行数
     */
    int insertIgnore(ServiceCost serviceCost);

    /**
     * 批量插入记录(忽略重复)
     * @param list 服务消费记录列表
     * @return 影响行数
     */
    int batchInsertIgnore(List<ServiceCost> list);
}