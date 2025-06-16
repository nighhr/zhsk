package com.zhonghe.apporder.service.Impl;

import cn.hutool.core.util.IdUtil;
import com.zhonghe.apporder.mapper.OrderLineMapper;
import com.zhonghe.apporder.mapper.OrderMapper;
import com.zhonghe.apporder.model.DTO.OrderCreateDTO;
import com.zhonghe.apporder.model.SalesOrder;
import com.zhonghe.apporder.model.SalesOrderLine;
import com.zhonghe.apporder.model.User;
import com.zhonghe.apporder.model.VO.SalesOrderVO;
import com.zhonghe.apporder.model.enums.OrderStatusEnum;
import com.zhonghe.apporder.service.OrderService;
import com.zhonghe.kernel.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderLineMapper orderLineMapper;

    @Override
    public PageResult<SalesOrder> getSalesOrderListByUser(Map<String, Object> params) {
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());

        // Calculate offset
        int offset = (page - 1) * pageSize;
        params.put("offset", offset);
        params.put("pageSize", pageSize);

        List<SalesOrder> salesOrders = orderMapper.selectSalesOrderListByUser(params);
        long total = orderMapper.selectSalesOrderCountByUser(params);

        PageResult<SalesOrder> pageResult = new PageResult<>();
        pageResult.setList(salesOrders);
        pageResult.setTotal(total);
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);
        return pageResult;
    }

    @Override
    public SalesOrderVO getOrderDetailById(String orderId, Long tenantId) {
        // 1. 获取订单基本信息
        SalesOrder order = orderMapper.selectByIdAndTenantId(orderId, tenantId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 获取订单行信息
        List<SalesOrderLine> orderLines = orderLineMapper.selectByOrderId(orderId, tenantId);

        // 3. 组装VO对象
        SalesOrderVO vo = new SalesOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setSalesOrderLineList(orderLines);

        return vo;
    }

    @Override
    public String createOrder(OrderCreateDTO orderCreateDTO, User currentUser) throws ParseException {
        // 1. 生成订单ID
        String orderId = IdUtil.simpleUUID();

        // 2. 构建订单主表对象
        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setTenantId(currentUser.getTenantId());
        order.setMemberId(currentUser.getId());
        order.setMemberName(currentUser.getUserName());
        order.setCustomerId(orderCreateDTO.getCustomerId());
        order.setCustomerName(orderCreateDTO.getCustomerName());
        order.setCustomerAddr(orderCreateDTO.getCustomerAddr());
        if (orderCreateDTO.getOrderStatus().equals("1")) {
            order.setOrderStatus(OrderStatusEnum.SAVED.getCode());
        } else {
            order.setOrderStatus(OrderStatusEnum.SUBMITTED.getCode());
        }

        SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置为 UTC
        String format = utcFormat.format(orderCreateDTO.getOrderDate());
        order.setOrderDate(utcFormat.parse(format));
        order.setMemo(orderCreateDTO.getMemo());
        order.setCreator(currentUser.getUserName());
        order.setCreateTime(new Date());

        // 3. 计算订单总金额
        BigDecimal totalAmount = orderCreateDTO.getOrderLines().stream()
                .map(line -> line.getPrice().multiply(line.getLineQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount.toString());

        // 4. 保存订单主表
        orderMapper.insert(order);

        // 5. 处理订单行
        List<SalesOrderLine> orderLines = orderCreateDTO.getOrderLines().stream()
                .map(lineDTO -> {
                    SalesOrderLine line = new SalesOrderLine();
                    line.setId(IdUtil.simpleUUID());
                    line.setTenantId(currentUser.getTenantId());
                    line.setOrderId(orderId);
                    line.setItemCode(lineDTO.getItemCode());
                    line.setItemFullName(lineDTO.getItemFullName());
                    line.setItemAbbrName(lineDTO.getItemAbbrName());
                    line.setImgUrl(lineDTO.getImgUrl());
                    line.setPrice(lineDTO.getPrice());
                    line.setLineQty(lineDTO.getLineQty());
                    line.setLineAmount(lineDTO.getPrice().multiply(lineDTO.getLineQty()));
                    line.setMemo(lineDTO.getMemo());
                    line.setCreator(currentUser.getUserName());
                    line.setCreateTime(new Date());
                    return line;
                })
                .collect(Collectors.toList());

        // 6. 批量插入订单行
        orderLineMapper.batchInsert(orderLines);

        return orderId;
    }

    @Override
    public SalesOrderVO updateOrder(OrderCreateDTO orderCreateDTO, User currentUser) {
        SalesOrderVO salesOrderVO = new SalesOrderVO();
        SalesOrder order = orderMapper.selectByIdAndTenantId(orderCreateDTO.getId(), currentUser.getTenantId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus()!=1){
            throw new RuntimeException("此订单已不可修改");
        }
        salesOrderVO.setId(orderCreateDTO.getId());
        salesOrderVO.setTenantId(currentUser.getTenantId());
        salesOrderVO.setCustomerId(orderCreateDTO.getCustomerId());
        salesOrderVO.setCustomerName(orderCreateDTO.getCustomerName());
        salesOrderVO.setCustomerAddr(orderCreateDTO.getCustomerAddr());
        if (orderCreateDTO.getOrderStatus().equals("1")) {
            salesOrderVO.setOrderStatus(OrderStatusEnum.SAVED.getCode());
        } else {
            salesOrderVO.setOrderStatus(OrderStatusEnum.SUBMITTED.getCode());
        }
        salesOrderVO.setOrderDate(orderCreateDTO.getOrderDate());
        salesOrderVO.setMemo(orderCreateDTO.getMemo());
        salesOrderVO.setUpdater(currentUser.getUserName());
        salesOrderVO.setUpdateTime(new Date());

        // 3. 计算订单总金额
        BigDecimal totalAmount = orderCreateDTO.getOrderLines().stream()
                .map(line -> line.getPrice().multiply(line.getLineQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        salesOrderVO.setTotalAmount(totalAmount.toString());

        orderMapper.update(salesOrderVO);

        if (orderCreateDTO.getOrderLines().isEmpty()){
            throw new RuntimeException("订单行不能为空");
        }

        List<SalesOrderLine> orderLines = orderCreateDTO.getOrderLines().stream()
                .map(lineDTO -> {
                    SalesOrderLine line = new SalesOrderLine();
                    line.setId(IdUtil.simpleUUID());
                    line.setTenantId(currentUser.getTenantId());
                    line.setOrderId(order.getId());
                    line.setItemCode(lineDTO.getItemCode());
                    line.setItemFullName(lineDTO.getItemFullName());
                    line.setItemAbbrName(lineDTO.getItemAbbrName());
                    line.setImgUrl(lineDTO.getImgUrl());
                    line.setPrice(lineDTO.getPrice());
                    line.setLineQty(lineDTO.getLineQty());
                    line.setLineAmount(lineDTO.getPrice().multiply(lineDTO.getLineQty()));
                    line.setMemo(lineDTO.getMemo());
                    line.setCreator(currentUser.getUserName());
                    line.setCreateTime(new Date());
                    return line;
                })
                .collect(Collectors.toList());
        orderLineMapper.deleteByOrderId(order.getId(),order.getTenantId(),currentUser.getUserName());
        orderLineMapper.batchInsert(orderLines);
        salesOrderVO.setSalesOrderLineList(orderLines);
        return salesOrderVO;
    }
}
