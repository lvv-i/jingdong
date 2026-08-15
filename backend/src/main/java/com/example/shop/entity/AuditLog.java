package com.example.shop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志表实体（数据字典 2.13 audit_logs，日志表省略 status）
 * T4 数据权限强制清单：管理员敏感操作必须写审计（operator_id/operator_role/target_type/target_id/action/remark）
 */
@Data
@TableName("audit_logs")
public class AuditLog {

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作者用户ID */
    private Long operatorId;

    /** 操作者角色：USER MERCHANT ADMIN */
    private String operatorRole;

    /** 目标类型：ORDER订单 REFUND退款 MERCHANT商家 PRODUCT商品 */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 动作：SHIP发货 APPROVE通过 REJECT驳回 HANDLE_REFUND退款裁决 TAKE_DOWN强制下架 */
    private String action;

    /** 备注（审核意见/裁决结果等） */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 软删除：0未删除 1已删除 */
    private Integer deletedFlag;
}
