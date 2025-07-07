package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Stock;
import com.zhonghe.backoffice.service.StockService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/list")
    public Result<PageResult<Stock>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("name", name);

        PageResult<Stock> pageResult = stockService.listStockByName(params);
        return Result.success(pageResult);
    }
    /**
     *  库存同步接口
     * */
    @GetMapping("/get")
    public Result<Integer> getStock() {

        return stockService.getStock();
    }
}
