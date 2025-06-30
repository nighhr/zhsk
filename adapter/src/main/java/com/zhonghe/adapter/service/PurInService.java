package com.zhonghe.adapter.service;


import com.zhonghe.adapter.response.AiTeResponse;

import java.util.Date;

public interface PurInService {

    AiTeResponse queryPurIn(Integer currentPage, Integer pageSize, String name, String code);

    void getPurIn(Integer currentPage, Integer pageSize,String start, String end);

}