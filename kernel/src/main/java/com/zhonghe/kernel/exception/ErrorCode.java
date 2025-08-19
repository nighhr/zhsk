package com.zhonghe.kernel.exception;

import lombok.Getter;

/**
 * 1: 通用错误
 *
 * 2: 登录(参数校验、权限等)
 *
 * 3: 业务逻辑错误
 *
 * 4: 服务端错误 (数据库、第三方服务等)
 *
 * 5: 系统级错误 (严重错误)
 * */
@Getter
public enum ErrorCode {
    SUCCESS(200, "成功"),
    // 通用错误(01)
    PARAM_ERROR(101, "参数错误"),

    // 登录错误 (02)
    UNAUTHORIZED(201, "登录未授权"),
    LOGIN_REQUIRED(202, "请先登录"),
    INVALID_TOKEN(203, "授权过期，请重新登录"),
    LOST_TOKEN(204, "系统错误，请重新登录"),
    BIND_USER(205, "首次登录 请输入手机号绑定"),
    BIND_REPEAT(206, "此手机号已绑定了其他微信"),
    USER_REJECT(207, "您不是本公司员工 请联系管理员处理"),
    WX_RES_FAILED(208,"解析微信响应失败"),

    // 业务逻辑错误 (03)
    ORDER_NOT_FOUND(301, "订单不存在"),
    ORDER_ACCESS_REJECT(302, "您无权访问该订单"),
    CUSTOMER_NOT_FOUND(303, "客户不存在"),
    VOUCHER_HEAD_NULL(304,"任务没有设置凭证头"),
    ORDER_LIST_NULL(305,"目标日期内没有订单"),

    // 服务端错误 (04)


    // 系统错误
    INTERNAL_ERROR(500, "系统内部错误"),
    DB_CONNECT_ERROR(501, "数据库连接错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}