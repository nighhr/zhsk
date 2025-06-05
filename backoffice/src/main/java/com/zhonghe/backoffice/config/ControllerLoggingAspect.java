package com.zhonghe.backoffice.config;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Controller层日志切面
 */
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);


    /**
     * 拦截所有Controller方法调用，记录请求信息
     */
    @Before("execution(* com.zhonghe.*.controller..*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 记录请求路径、方法、IP
            logger.info("请求URL: {}", request.getRequestURL());
            logger.info("HTTP方法: {}", request.getMethod());
            logger.info("客户端IP: {}", request.getRemoteAddr());
        }

        // 记录调用的类和方法
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("调用方法: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());

        // 记录参数（排除HttpServletRequest/Response等对象）
        Object[] args = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse))
                .toArray();

        if (args.length > 0) {
            logger.info("方法参数: {}", Arrays.toString(args));
        } else {
            logger.info("方法无参数或参数已过滤");
        }
    }
}