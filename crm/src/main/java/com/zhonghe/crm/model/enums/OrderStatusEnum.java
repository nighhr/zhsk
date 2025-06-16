package com.zhonghe.crm.model.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {
    SAVED(1, "保存", "订单已保存但未提交"),
    SUBMITTED(2, "提交", "订单已提交等待审核"),
    UNDER_REVIEW(3, "审核中", "订单正在审核流程中"),
    APPROVED(4, "审核通过", "订单已审核通过");

    private final int code;
    private final String name;
    private final String detail;

    private static final Map<Integer, OrderStatusEnum> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(OrderStatusEnum::getCode, e -> e));

    OrderStatusEnum(int code, String name, String detail) {
        this.code = code;
        this.name = name;
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * 根据code获取枚举
     */
    public static OrderStatusEnum fromCode(int code) {
        OrderStatusEnum status = CODE_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订单状态码: " + code);
        }
        return status;
    }

    /**
     * 检查是否允许转换为目标状态
     */
    public boolean canTransferTo(OrderStatusEnum targetStatus) {
        switch (this) {
            case SAVED:
                return targetStatus == SUBMITTED;
            case SUBMITTED:
                return targetStatus == UNDER_REVIEW || targetStatus == SAVED;
            case UNDER_REVIEW:
                return targetStatus == APPROVED || targetStatus == SUBMITTED;
            case APPROVED:
                return false; // 审核通过后不能再修改状态
            default:
                return false;
        }
    }
}