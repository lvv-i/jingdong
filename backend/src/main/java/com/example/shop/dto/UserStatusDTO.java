package com.example.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员修改用户状态请求（T5 A-017：{status: NORMAL/DISABLED}）
 */
@Data
public class UserStatusDTO {

    @NotNull(message = "status 不能为空")
    @Pattern(regexp = "NORMAL|DISABLED", message = "status 只能为 NORMAL 或 DISABLED")
    private String status;
}
