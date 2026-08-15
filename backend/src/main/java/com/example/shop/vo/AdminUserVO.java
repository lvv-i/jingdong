package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员用户列表项（T5 A-016：{id, username, phone, role, status}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {

    private Long id;

    private String username;

    private String phone;

    /** 角色：USER MERCHANT ADMIN */
    private String role;

    /** 状态：NORMAL DISABLED */
    private String status;
}
