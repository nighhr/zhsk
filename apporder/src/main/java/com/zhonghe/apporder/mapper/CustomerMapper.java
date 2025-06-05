package com.zhonghe.apporder.mapper;

import com.zhonghe.apporder.model.VO.CustomerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {
    List<CustomerVO> selectCustomersByMemberId(@Param("memberId") String memberId);
}
