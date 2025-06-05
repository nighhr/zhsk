package com.zhonghe.adapter.service;


import com.zhonghe.adapter.response.PurInResponse;

public interface PurInService {


    PurInResponse queryPurIn(Integer currentPage, Integer pageSize, String name, String code);
}