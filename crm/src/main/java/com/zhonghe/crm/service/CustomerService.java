package com.zhonghe.crm.service;


import com.zhonghe.crm.model.VO.CustomerVO;

import java.util.List;

public interface CustomerService {
    List<CustomerVO> getCustomersByMemberId(String memberId) ;
}
