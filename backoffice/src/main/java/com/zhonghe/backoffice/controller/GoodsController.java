package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Goods;
import com.zhonghe.backoffice.service.GoodsService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    @GetMapping("/list")
    public Result<PageResult<Goods>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("name", name);

        PageResult<Goods> pageResult = goodsService.listGoodsByName(params);
        return Result.success(pageResult);
    }

    /**
     *  商品同步接口
     * */
    @GetMapping("/get")
    public Result<Integer> getDeptData() {
        return goodsService.getGoods();
    }
}
