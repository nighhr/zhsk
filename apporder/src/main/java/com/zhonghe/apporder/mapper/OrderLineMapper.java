package com.zhonghe.apporder.mapper;

import com.zhonghe.apporder.model.SalesOrderLine;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderLineMapper {
    List<SalesOrderLine> selectByOrderId(@Param("orderId") String orderId,
                                         @Param("tenantId") Long tenantId);

    void batchInsert(List<SalesOrderLine> orderLines);

    /**
     * 软删除指定订单的所有订单行
     *
     * @param orderId  订单ID
     * @param tenantId 租户ID
     * @param updater  更新人
     */
    void deleteByOrderId(@Param("orderId") String orderId,
                         @Param("tenantId") Long tenantId,
                         @Param("updater") String updater);
}

