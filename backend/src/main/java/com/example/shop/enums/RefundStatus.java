package com.example.shop.enums;

import lombok.Getter;

/**
 * 售后单状态枚举（T1 状态机 v1.0：售后 6 态）
 * 终态：REFUNDED 已退款 / CLOSED 已关闭
 */
@Getter
public enum RefundStatus {

    REFUNDING("退款中"),
    MERCHANT_AGREED("商家同意"),
    MERCHANT_REJECTED("商家拒绝"),
    ADMIN_INTERVENED("平台介入"),
    REFUNDED("已退款"),
    CLOSED("已关闭");

    private final String label;

    RefundStatus(String label) {
        this.label = label;
    }

    public static boolean isValid(String value) {
        for (RefundStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
