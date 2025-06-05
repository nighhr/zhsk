package com.zhonghe.apporder.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.apporder.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.zhonghe.kernel.vo.Result;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${wechat.appid}")
    private String APP_ID;

    @Value("${wechat.appsecret}")
    private String APP_SECRET;

    /**
     * 登录操作
     * 先校验openId是否在业务员表 如果有就返回用户信息 如果没有就返回空
     */

    @PostMapping("/login")
    public Result login(@RequestBody AuthRequest params) {
        String code = params.getCode();
        return userService.login(code);
    }


    /**
     * 注册操作
     * 先校验手机号是否在业务员表 如果没有就返回空提示无权限 如果有就把openId与业务员绑定
     */
    @PostMapping("/register")
    public Result register(@RequestBody AuthRequest params){

        return userService.register(params.getCode(), params.getMobile());
    }



    private JsonNode getWechatSession(String code) throws JsonProcessingException {
        // 用code换取access_token和openid
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=" + APP_ID +
                "&secret=" + APP_SECRET +
                "&code=" + code +
                "&grant_type=authorization_code";

        // 发送HTTP请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // 解析返回的JSON
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.getBody());
    }

    @Data
    public static class AuthRequest {
        private String code;
        private String mobile;
    }
}
