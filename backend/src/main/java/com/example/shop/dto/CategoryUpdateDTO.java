package com.example.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员编辑类目请求（T5 A-006：{name, sortOrder, status}）
 */
@Data
public class CategoryUpdateDTO {

    @Size(max = 50, message = "类目名称过长")
    private String name;

    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortOrder;

    /** 状态：ENABLED启用 DISABLED禁用 */
    @Pattern(regexp = "ENABLED|DISABLED", message = "status 只能为 ENABLED 或 DISABLED")
    private String status;
}
