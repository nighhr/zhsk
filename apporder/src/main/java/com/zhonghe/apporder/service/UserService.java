package com.zhonghe.apporder.service;


import com.zhonghe.apporder.model.User;
import com.zhonghe.kernel.vo.Result;

public interface UserService {

    Result login(String openid);

    Result register(String code, String mobileNo);

    User getCurrentUser();

}

