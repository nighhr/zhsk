package com.zhonghe.crm.mapper;

import com.zhonghe.crm.model.SalesOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    List<SalesOrder> selectSalesOrderListByUser(Map<String, Object> params);

    long selectSalesOrderCountByUser(Map<String, Object> params);

    SalesOrder selectByIdAndTenantId(@Param("id") String id, @Param("tenantId") Long tenantId);

    void insert(SalesOrder order);

    void update(SalesOrder order);
}

