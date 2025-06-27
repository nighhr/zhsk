package com.zhonghe.adapter.controller;

import com.zhonghe.adapter.response.AiTeResponse;
import com.zhonghe.adapter.service.PurInService;
import com.zhonghe.kernel.vo.request.ApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/aiTe")
public class AiTeController {

    @Autowired
    private PurInService purInService;

    /**
     * 采购入库接口
     */
    @PostMapping("/getPurIn")
    public void getPurInData(@RequestBody ApiRequest aiTeRequest) {
        purInService.getPurIn(aiTeRequest.getCurrent_page(), aiTeRequest.getPage_size(), aiTeRequest.getStart(), aiTeRequest.getEnd());
    }



    @PostMapping("/pur")
    public AiTeResponse getPurInTest() {
        return purInService.queryPurIn(1, 50, "", "");
    }
}
