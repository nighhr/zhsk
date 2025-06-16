package com.zhonghe.crm.controller;

import com.zhonghe.crm.model.VO.CustomerVO;
import com.zhonghe.crm.service.CustomerService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/listByMember")
    public Result listCustomersByMember(@RequestParam String memberId) {
        try {
            List<CustomerVO> customersByMemberId = customerService.getCustomersByMemberId(memberId);
            return Result.success(customersByMemberId);
        }catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }

    }


}
