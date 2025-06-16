package com.zhonghe.crm.controller;

import com.zhonghe.crm.model.User;
import com.zhonghe.crm.model.VO.ItemVO;
import com.zhonghe.crm.service.ItemService;
import com.zhonghe.crm.service.UserService;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<ItemVO>> getItemList() {

        User currentUser = userService.getCurrentUser();
        List<ItemVO> items = itemService.getItemsByMember(
                currentUser.getTenantId());

        return Result.success(items);
    }
}


