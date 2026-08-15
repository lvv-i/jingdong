package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应（T5 P-002/P-008：{token, userInfo}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** JWT token（Authorization: Bearer <token>） */
    private String token;

    /** 用户信息（id/username/role/shopId） */
    private UserInfoVO userInfo;
}
