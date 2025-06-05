package com.zhonghe.adapter.controller;

import com.zhonghe.adapter.response.PurInResponse;
import com.zhonghe.adapter.service.PurInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aiTe")
public class AiTeController {

    @Autowired
    private PurInService purInService;

    @PostMapping("/pur")
    public PurInResponse getDeptData() {
        return purInService.queryPurIn(1, 50, "", "");
    }

}
