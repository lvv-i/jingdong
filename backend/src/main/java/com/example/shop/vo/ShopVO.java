package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺信息（T5 M-001：{id, shopName, categoryId, description, auditStatus, auditReason}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO {

    private Long id;

    private String shopName;

    /** 主营类目ID */
    private Long categoryId;

    private String description;

    /** 入驻审核状态：PENDING_AUDIT APPROVED REJECTED */
    private String auditStatus;

    /** 管理员审核意见/驳回原因 */
    private String auditReason;
}
