package com.zhonghe.crm.service.Impl;

import com.zhonghe.crm.mapper.CustomerMapper;
import com.zhonghe.crm.model.VO.CustomerVO;
import com.zhonghe.crm.service.CustomerService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public List<CustomerVO> getCustomersByMemberId(String memberId) {
        if (memberId.isEmpty()){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return customerMapper.selectCustomersByMemberId(memberId);
    }
}
