package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家审核列表项（T5 A-001：{id, shopName, auditStatus, auditReason, userPhone}）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMerchantVO {

    private Long id;

    private String shopName;

    /** 入驻审核状态：PENDING_AUDIT APPROVED REJECTED */
    private String auditStatus;

    private String auditReason;

    /** 商家账号手机号 */
    private String userPhone;
}
