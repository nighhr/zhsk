package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Goods;
import com.zhonghe.kernel.vo.Result;

import java.util.List;
import java.util.Map;

public interface GoodsService {

    Result<List<Goods>> searchItem(Map<String, Object> params);

    Result<Integer> getGoods();
}
