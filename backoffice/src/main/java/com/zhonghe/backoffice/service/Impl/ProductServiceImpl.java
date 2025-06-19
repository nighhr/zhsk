package com.zhonghe.backoffice.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.zhonghe.backoffice.mapper.ProductMapper;
import com.zhonghe.backoffice.model.Product;
import com.zhonghe.backoffice.service.ProductService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<Product> products = new ArrayList<>();
        if (params.get("name")==null){
             products = productMapper.selectProducts(null,offset,pageSize);
        }else {
             products = productMapper.selectProducts(params.get("name").toString(),offset,pageSize);

        }
        return Result.success(products);
    }
}
