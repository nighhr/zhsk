package com.zhonghe.kernel.context;

/**
 *  用户上下文控制器
 * */
public class UserContext {
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(String openId) {
        currentUser.set(openId);
    }

    public static String getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}