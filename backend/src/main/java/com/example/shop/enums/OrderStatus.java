package com.example.shop.enums;

import lombok.Getter;

/**
 * 订单状态枚举（T1 状态机 v1.0：订单 5 态）
 * 状态值禁止数字码、禁止前端自创；非法流转由 Service 层校验拒绝
 */
@Getter
public enum OrderStatus {

    PENDING_PAY("待支付"),
    PAID("已支付待发货"),
    SHIPPED("已发货待收货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    /** 状态值是否合法（防脏数据） */
    public static boolean isValid(String value) {
        for (OrderStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
