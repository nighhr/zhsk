package com.zhonghe.crm.mapper;

import com.zhonghe.crm.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface UserMapper {

    ArrayList<User> selectSalesmanByOpenId(@Param("openId")String openId);

    ArrayList<User> selectSalesmanByMobileNo(@Param("mobileNo")String mobileNo);

    Boolean updateOpenIdByMobileNo(@Param("openId")String openId,@Param("mobileNo")String mobileNo,@Param("tenantId")Long tenantId);

    User selectById(@Param("id")String id);

}