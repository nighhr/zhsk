package com.zhonghe.crm.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhonghe.crm.mapper.UserMapper;
import com.zhonghe.crm.model.DTO.LoginResult;
import com.zhonghe.crm.model.DTO.WxSessionDTO;
import com.zhonghe.crm.model.User;
import com.zhonghe.crm.service.UserService;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.exception.UnauthorizedException;
import com.zhonghe.kernel.utils.JwtUtil;
import com.zhonghe.kernel.vo.Result;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {

    @Value("${wechat.appid}")
    private String APP_ID;

    @Value("${wechat.appsecret}")
    private String APP_SECRET;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private View error;


    @Override
    public Result login(String code) {
        try {
            //查询业务员表
            WxSessionDTO wxSessionDTO = wxLogin(code);
            ArrayList<User> users = userMapper.selectSalesmanByOpenId(wxSessionDTO.getOpenid());
//            ArrayList<User> users = userMapper.selectSalesmanByOpenId("123123");
            //如果没有查询到数据 返回为首次绑定 让其输入手机号验证校验
            if (users.isEmpty()) {
                throw new BusinessException(ErrorCode.BIND_USER);
            }
            //如果查询到数据 则以查询到的第一条数据直接登录
            User user = users.get(0);
            // 3. 生成JWT
            String token = jwtUtil.generateToken(user.getId().toString());

            return Result.success(new LoginResult(user, token));
        } catch (BusinessException  e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }

    }

    @Override
    public Result register(String code, String mobileNo) {
        try {
            WxSessionDTO wxSessionDTO = wxLogin(code);
            ArrayList<User> users = userMapper.selectSalesmanByMobileNo(mobileNo);
//            ArrayList<User> users = userMapper.selectSalesmanByMobileNo(mobileNo);
            //如果没有查询到数据 需要报错 您不是本公司员工 请联系管理员处理
            if (users.isEmpty()) {
                throw new BusinessException(ErrorCode.USER_REJECT);
            }
            //todo 是否一个手机号有多个业务员 实际业务需要问好
            User user = users.get(0);
            if (user.getOpenId()==null|| user.getOpenId().isEmpty()) {
                userMapper.updateOpenIdByMobileNo(wxSessionDTO.getOpenid(), mobileNo, user.getTenantId());
            } else {
                //todo 如果用户已绑定了其他微信 是否支持解绑和换绑
                throw new BusinessException(ErrorCode.BIND_REPEAT);
            }
            //更新成功则返回用户
            String token = jwtUtil.generateToken(user.getId().toString());
            return Result.success(new LoginResult(user, token));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }


    }

    @Override
    public User getCurrentUser() {
        // 1. 从请求头获取token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("缺少有效的认证令牌");
        }

        // 2. 解析token
        token = token.substring(7); // 去掉"Bearer "前缀
        String userId;
        try{
            Claims claims = jwtUtil.parseToken(token);
            // 3. 从token中获取用户ID
            userId = claims.get("id").toString();

        }catch (Exception e) {
            throw new UnauthorizedException("登录超时，请重新登录");
        }



        // 4. 查询数据库获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在或已被删除");
        }

        return user;
    }

    private WxSessionDTO wxLogin(String code) {
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                APP_ID, APP_SECRET, code);


        String response = restTemplate.getForObject(url, String.class);

        // 手动转换为对象
        ObjectMapper mapper = new ObjectMapper();
        try {
            WxSessionDTO wxSession = mapper.readValue(response, WxSessionDTO.class);
            if (wxSession.getSession_key() == null || wxSession.getOpenid() == null) {
                throw new UnauthorizedException("获取openId异常");
            }
            return wxSession;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.WX_RES_FAILED,"解析微信响应失败");
        }
    }


}
