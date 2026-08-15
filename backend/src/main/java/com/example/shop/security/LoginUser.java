package com.example.shop.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录人信息（JWT claims 解析结果）
 * 角色常量：USER 普通用户 / MERCHANT 商家 / ADMIN 管理员（T4 JWT/RBAC 方案）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_ADMIN = "ADMIN";

    /** 用户ID（claims.sub） */
    private Long id;

    /** 用户名 */
    private String username;

    /** 角色：USER / MERCHANT / ADMIN */
    private String role;

    /** 店铺ID（仅商家签发时携带） */
    private Long shopId;
}
