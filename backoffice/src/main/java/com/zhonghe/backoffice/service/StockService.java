package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Stock;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;

import java.util.Map;

public interface StockService {
    Result<Integer> getStock();

    PageResult<Stock> listStockByName(Map<String, Object> params);
}
