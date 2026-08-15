package com.example.shop.security;

/**
 * 当前登录人上下文（ThreadLocal 实现）
 * 用法：拦截器校验 token 后 set；Service 层通过 UserContext.get() 获取当前用户做数据权限校验；
 * 请求结束后由拦截器 afterCompletion 清理，避免线程池复用导致串号
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /** 获取当前登录人；未登录返回 null */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前用户ID；未登录抛 1002（数据权限校验入口用） */
    public static Long requireUserId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new com.example.shop.common.BusinessException(com.example.shop.common.ErrorCode.NOT_LOGIN);
        }
        return user.getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
