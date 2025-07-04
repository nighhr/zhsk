package com.zhonghe.kernel.vo;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 成功静态方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 成功静态方法
    public static <T> Result<T> fail(T data) {
        Result<T> result = new Result<>();
        result.setCode(201);
        result.setMessage("fail");
        result.setData(data);
        return result;
    }
    // 错误静态方法
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}