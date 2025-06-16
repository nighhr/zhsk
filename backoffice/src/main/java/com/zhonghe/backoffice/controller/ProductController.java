package com.zhonghe.backoffice.controller;

import com.zhonghe.backoffice.model.Item;
import com.zhonghe.backoffice.service.ItemService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aiTe")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     *  商品创建接口
     * */
    @PostMapping("/item/create")
    public Result<List<Item>> searchItem(Map<String, Object> params) {

        return itemService.searchItem(params);
    }
}
