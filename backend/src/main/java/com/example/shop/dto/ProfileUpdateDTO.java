package com.example.shop.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料请求（T5 U-002：{phone, oldPassword?, newPassword?}）
 * 改密需原密码校验（T5 备注）
 */
@Data
public class ProfileUpdateDTO {

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 原密码（改密时必填） */
    private String oldPassword;

    /** 新密码（改密时必填） */
    @Size(min = 6, max = 20, message = "新密码长度 6-20 位")
    private String newPassword;
}
