package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志列表项（T5 A-018：
 * {id, operatorId, operatorRole, targetType, targetId, action, remark, createdAt}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogVO {

    private Long id;

    /** 操作者用户ID */
    private Long operatorId;

    /** 操作者角色：USER MERCHANT ADMIN */
    private String operatorRole;

    /** 目标类型：ORDER REFUND MERCHANT PRODUCT */
    private String targetType;

    private Long targetId;

    /** 动作：SHIP AGREE REJECT HANDLE_REFUND APPROVE TAKE_DOWN */
    private String action;

    private String remark;

    private LocalDateTime createdAt;
}
