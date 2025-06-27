package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Goods;
import com.zhonghe.backoffice.service.GoodsService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/product")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     *  商品查询接口
     * */
    @PostMapping("/list")
    public Result<List<Goods>> searchItem(@RequestBody Map<String, Object> params) {

        return goodsService.searchItem(params);
    }

    /**
     *  商品同步接口
     * */
    @GetMapping("/get")
    public Result<Integer> getDeptData() {
        return goodsService.getGoods();
    }
}
