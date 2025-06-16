package com.zhonghe.backoffice.service;

import com.zhonghe.backoffice.model.Product;
import com.zhonghe.kernel.vo.Result;

import java.util.List;
import java.util.Map;

public interface ProductService {

    Result<List<Product>> searchItem(Map<String, Object> params);
}
