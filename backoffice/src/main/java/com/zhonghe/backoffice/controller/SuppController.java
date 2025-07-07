package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Supplier;
import com.zhonghe.backoffice.service.SuppService;
import com.zhonghe.kernel.vo.PageResult;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/supplier")
public class SuppController {

    @Autowired
    private SuppService suppService;

    @GetMapping("/list")
    public Result<PageResult<Supplier>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("pageSize", pageSize);
        params.put("name", name);

        PageResult<Supplier> pageResult = suppService.listSupplierByName(params);
        return Result.success(pageResult);
    }

    /**
     *  供应商同步接口
     * */
    @GetMapping("/get")
    public Result<Integer> getSupplier() {

        return suppService.getSupp();
    }
}
