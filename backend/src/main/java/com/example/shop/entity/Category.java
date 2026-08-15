package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品类目表实体（数据字典 2.3 categories）
 * parent_id 自关联（0 为顶级类目）；status：ENABLED启用 DISABLED禁用
 */
@Data
@TableName("categories")
public class Category {

    /** 类目ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父类目ID（0为顶级类目） */
    private Long parentId;

    /** 类目名称 */
    private String name;

    /** 排序值（越小越靠前） */
    private Integer sortOrder;

    /** 状态：ENABLED DISABLED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
