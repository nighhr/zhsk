package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Item;
import com.zhonghe.kernel.vo.Result;

import java.util.List;
import java.util.Map;

public interface ItemService {

    Result<List<Item>> searchItem(Map<String, Object> params);
}
