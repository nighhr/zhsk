package com.zhonghe.crm.model.DTO;

import com.zhonghe.crm.model.User;
import lombok.Data;

@Data
public class LoginResult {
    private User user;
    private String token;

    public LoginResult(User user, String token) {
        this.user = user;
        this.token = token;
    }
}
