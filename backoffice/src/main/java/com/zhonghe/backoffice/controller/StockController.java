package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.service.StockService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backoffice/stock")
public class StockController {

    @Autowired
    private StockService stockService;
    /**
     *  供应商同步接口
     * */
    @GetMapping("/get")
    public Result<Integer> getStock() {

        return stockService.getStock();
    }
}
