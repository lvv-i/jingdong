package com.example.shop.enums;

import lombok.Getter;

/**
 * 商品状态枚举（T1 状态机 v1.0：商品 4 态审核制）
 * 合法流转：DRAFT→PENDING_ON_SALE（商家提交）、PENDING_ON_SALE→ON_SALE（管理员通过）、
 * PENDING_ON_SALE→DRAFT（管理员驳回）、ON_SALE→OFF_SALE（商家下架/管理员强制下架）、
 * OFF_SALE→PENDING_ON_SALE（修改重提）
 */
@Getter
public enum ProductStatus {

    DRAFT("草稿"),
    PENDING_ON_SALE("待上架"),
    ON_SALE("已上架"),
    OFF_SALE("已下架");

    private final String label;

    ProductStatus(String label) {
        this.label = label;
    }

    public static boolean isValid(String value) {
        for (ProductStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
