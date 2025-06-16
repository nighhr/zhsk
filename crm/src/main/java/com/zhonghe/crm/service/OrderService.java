package com.zhonghe.apporder.service;

import com.zhonghe.apporder.model.DTO.OrderCreateDTO;
import com.zhonghe.apporder.model.SalesOrder;
import com.zhonghe.apporder.model.User;
import com.zhonghe.apporder.model.VO.SalesOrderVO;
import com.zhonghe.kernel.vo.PageResult;

import java.text.ParseException;
import java.util.Map;

public interface OrderService {


    PageResult<SalesOrder> getSalesOrderListByUser(Map<String, Object> params);
    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @param tenantId 租户ID
     * @return 订单详情VO
     */
    SalesOrderVO getOrderDetailById(String orderId, Long tenantId);

    String createOrder(OrderCreateDTO orderCreateDTO, User currentUser) throws ParseException;

    SalesOrderVO updateOrder(OrderCreateDTO orderCreateDTO, User currentUser);
}
