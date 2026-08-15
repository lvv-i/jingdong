package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户信息（T5 P-002：{id, username, role, shopId}）
 * shopId 仅商家登录时携带（无店铺返回 6001）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    private Long id;

    private String username;

    /** 角色：USER MERCHANT ADMIN */
    private String role;

    /** 店铺ID（仅商家） */
    private Long shopId;
}
