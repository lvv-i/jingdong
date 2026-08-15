package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类目信息（T5 P-003：{id, parentId, name, sortOrder}；A-004 含 status）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {

    private Long id;

    /** 父类目ID（0为顶级类目） */
    private Long parentId;

    private String name;

    /** 排序值（越小越靠前） */
    private Integer sortOrder;

    /** 状态：ENABLED DISABLED（仅管理员接口返回） */
    private String status;
}
