package com.example.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员创建类目请求（T5 A-005：{parentId, name, sortOrder}）
 */
@Data
public class CategoryCreateDTO {

    /** 父类目ID（0 或空为顶级类目） */
    private Long parentId;

    @NotBlank(message = "类目名称不能为空")
    @Size(max = 50, message = "类目名称过长")
    private String name;

    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortOrder;
}
