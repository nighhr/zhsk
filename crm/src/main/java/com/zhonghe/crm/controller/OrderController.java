package com.zhonghe.apporder.controller;

import com.zhonghe.apporder.model.DTO.OrderCreateDTO;
import com.zhonghe.apporder.model.SalesOrder;
import com.zhonghe.apporder.model.User;
import com.zhonghe.apporder.model.VO.SalesOrderVO;
import com.zhonghe.apporder.service.OrderService;
import com.zhonghe.apporder.service.UserService;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping("/list")
    public Result<PageResult<SalesOrder>> getOrderList(@RequestBody Map<String, Object> params) {
        try {
            User currentUser = userService.getCurrentUser();

            params.put("userId", currentUser.getId());
            params.put("tenantId", currentUser.getTenantId());
            PageResult<SalesOrder> pageResult = orderService.getSalesOrderListByUser(params);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(300, "获取订单列表失败: " + e.getMessage());
        }
    }
    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return 订单详情(包含订单行信息)
     */
    @GetMapping("/{orderId}")
    public Result<SalesOrderVO> getOrderDetail(@PathVariable String orderId) {
        try {
            User currentUser = userService.getCurrentUser();
            SalesOrderVO orderDetail = orderService.getOrderDetailById(orderId, currentUser.getTenantId());

            // 权限校验：确保用户只能访问自己的订单
            if (!orderDetail.getMemberId().equals(currentUser.getId())) {
                return Result.error(ErrorCode.ORDER_ACCESS_REJECT.getCode(),ErrorCode.ORDER_ACCESS_REJECT.getMessage());
            }

            return Result.success(orderDetail);
        } catch (Exception e) {
            return Result.error(300, "获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增订单
     * @param orderCreateDTO 订单创建DTO
     * @return 创建结果
     */
    @PostMapping("insert")
    public Result<String> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        try {
            User currentUser = userService.getCurrentUser();
            String orderId = orderService.createOrder(orderCreateDTO, currentUser);
            return Result.success(orderId);
        } catch (Exception e) {
            return Result.error(300, "创建订单失败: " + e.getMessage());
        }
    }


    @PostMapping("update")
    public Result<SalesOrderVO> updateOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        log.info("Received DTO: {}", orderCreateDTO);
        try {
            User currentUser = userService.getCurrentUser();
            SalesOrderVO salesOrderVO = orderService.updateOrder(orderCreateDTO, currentUser);
            return Result.success(salesOrderVO);
        }catch (Exception e) {
            return Result.error(300, "修改订单失败: " + e.getMessage());
        }
    }


}
