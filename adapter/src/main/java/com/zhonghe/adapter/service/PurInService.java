package com.zhonghe.adapter.service;


import com.zhonghe.adapter.model.FOrder;
import com.zhonghe.kernel.vo.Result;

import java.util.List;

public interface PurInService {

    Result<List<FOrder>> queryPurIn(Integer currentPage, Integer pageSize, String name, String code);
}