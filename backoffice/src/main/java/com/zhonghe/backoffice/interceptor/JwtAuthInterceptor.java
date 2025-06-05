package com.zhonghe.backoffice.interceptor;

import cn.hutool.core.util.StrUtil;
import com.zhonghe.kernel.context.UserContext;
import com.zhonghe.kernel.exception.BusinessException;
import com.zhonghe.kernel.exception.ErrorCode;
import com.zhonghe.kernel.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtAuthInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("拦截请求: {}", uri);
        // 1. 从header获取token
        String token = request.getHeader("Authorization");
        if (StrUtil.isEmpty(token)) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
        // 去掉 "Bearer " 前缀（如果存在）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 2. 验证token
        if (!JwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 3. 解析并设置用户上下文
        Claims claims = JwtUtil.parseToken(token);
        String openId = claims.get("id", String.class);
        UserContext.setCurrentUser(openId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}