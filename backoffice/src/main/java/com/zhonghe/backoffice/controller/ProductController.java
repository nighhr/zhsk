package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Product;
import com.zhonghe.backoffice.service.ProductService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     *  商品创建接口
     * */
    @PostMapping("/list")
    public Result<List<Product>> searchItem(@RequestBody Map<String, Object> params) {

        return productService.searchItem(params);
    }
}
