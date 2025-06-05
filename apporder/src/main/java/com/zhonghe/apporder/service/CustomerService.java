package com.zhonghe.apporder.service;


import com.zhonghe.apporder.model.VO.CustomerVO;

import java.util.List;

public interface CustomerService {
    List<CustomerVO> getCustomersByMemberId(String memberId) ;
}
