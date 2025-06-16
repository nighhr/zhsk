package com.zhonghe.backoffice.service.Impl;

import com.zhonghe.backoffice.mapper.ProductMapper;
import com.zhonghe.backoffice.model.Product;
import com.zhonghe.backoffice.service.ProductService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Result<List<Product>> searchItem(Map<String, Object> params) {
        int page = params.get("page") == null ? 1 : Integer.parseInt(params.get("page").toString());
        int pageSize = params.get("pageSize") == null ? 10 : Integer.parseInt(params.get("pageSize").toString());

        int offset = (page - 1) * pageSize;
        params.put("offset", offset);
        params.put("pageSize", pageSize);
        List<Product> products = productMapper.selectProducts(params);
        return Result.success(products);
    }
}
