package com.example.shop.enums;

import lombok.Getter;

/**
 * 商家入驻审核状态枚举（T1 状态机 v1.0：入驻 3 态）
 * 合法流转：PENDING_AUDIT→APPROVED / PENDING_AUDIT→REJECTED / REJECTED→PENDING_AUDIT（修改重提）
 */
@Getter
public enum MerchantAuditStatus {

    PENDING_AUDIT("待审核"),
    APPROVED("已通过"),
    REJECTED("已驳回");

    private final String label;

    MerchantAuditStatus(String label) {
        this.label = label;
    }

    public static boolean isValid(String value) {
        for (MerchantAuditStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
