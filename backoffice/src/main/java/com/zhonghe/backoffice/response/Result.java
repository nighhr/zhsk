package com.zhonghe.backoffice.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result<T> {
    private String OFlag;
    private String Message;
    private T Data;

    // 成功静态方法
    public static <T> com.zhonghe.kernel.vo.Result<T> success(T data) {
        com.zhonghe.kernel.vo.Result<T> result = new com.zhonghe.kernel.vo.Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 错误静态方法
    public static <T> com.zhonghe.kernel.vo.Result<T> error(int code, String message) {
        com.zhonghe.kernel.vo.Result<T> result = new com.zhonghe.kernel.vo.Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}


