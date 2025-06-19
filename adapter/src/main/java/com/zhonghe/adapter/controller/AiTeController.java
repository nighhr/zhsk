package com.zhonghe.adapter.controller;

import com.zhonghe.adapter.response.AiTeResponse;
import com.zhonghe.adapter.service.GoodService;
import com.zhonghe.adapter.service.PurInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aiTe")
public class AiTeController {

    @Autowired
    private PurInService purInService;

    /**
     *  采购入库接口
     * */
    @PostMapping("/pur")
    public AiTeResponse getPurInData() {
        return purInService.queryPurIn(1, 50, "", "");
    }


}
