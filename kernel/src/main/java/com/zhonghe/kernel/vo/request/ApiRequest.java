package com.zhonghe.kernel.vo.request;

import lombok.Data;

import java.util.Date;

@Data
public class ApiRequest {
    private Integer current_page = 1;
    private Integer page_size = 50;
    private String start;
    private String end;
    // 构造器
    public ApiRequest(Integer current_page, Integer page_size) {
        this.current_page = current_page;
        this.page_size = page_size;
    }
}