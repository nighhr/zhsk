package com.zhonghe.crm.mapper;

import com.zhonghe.crm.model.VO.CustomerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {
    List<CustomerVO> selectCustomersByMemberId(@Param("memberId") String memberId);
}
