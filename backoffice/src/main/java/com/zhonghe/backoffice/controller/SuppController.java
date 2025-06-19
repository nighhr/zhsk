package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Supplier;
import com.zhonghe.backoffice.service.SuppService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backoffice/supplier")
public class SuppController {

    @Autowired
    private SuppService suppService;
    /**
     *  供应商同步接口
     * */
    @PostMapping("/get")
    public Result<Integer> getSupplier() {

        return suppService.getSupp();
    }
}
