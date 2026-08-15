package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料（T5 U-001：{id, username, phone, role}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private Long id;

    private String username;

    private String phone;

    /** 角色：USER MERCHANT ADMIN */
    private String role;
}
