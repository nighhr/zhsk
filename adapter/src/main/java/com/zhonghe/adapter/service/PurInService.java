package com.zhonghe.adapter.service;


import com.zhonghe.adapter.model.FOrder;
import com.zhonghe.adapter.response.AiTeResponse;
import com.zhonghe.kernel.vo.Result;

import java.util.List;

public interface PurInService {

    AiTeResponse queryPurIn(Integer currentPage, Integer pageSize, String name, String code);
}