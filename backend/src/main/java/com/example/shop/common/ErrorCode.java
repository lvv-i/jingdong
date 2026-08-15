package com.example.shop.common;

import lombok.Getter;

/**
 * 错误码枚举（唯一依据：docs/phase1/member-a/deliverables/03-错误码分段表.md T3 v1.0）
 *
 * 分段：公共 1000-1099 / 用户 2000-2099 / 商品 3000-3099 / 订单 4000-4099 /
 *       售后 5000-5099 / 商家 6000-6099 / 管理员 7000-7099
 * 使用：throw new BusinessException(ErrorCode.XXX)；由 GlobalExceptionHandler 统一转换
 */
@Getter
public enum ErrorCode {

    // ---------- 公共段 1000-1099 ----------
    SUCCESS(200, "成功"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_LOGIN(1002, "未登录"),
    NO_PERMISSION(1003, "无权限"),
    RESOURCE_NOT_FOUND(1004, "资源不存在"),
    SYSTEM_ERROR(1005, "系统错误"),
    DUPLICATE_SUBMIT(1006, "重复提交"),
    TOO_FREQUENT(1007, "操作过于频繁"),

    // ---------- 用户段 2000-2099 ----------
    USERNAME_EXISTS(2001, "用户名已存在"),
    PHONE_EXISTS(2002, "手机号已注册"),
    LOGIN_FAILED(2003, "用户名或密码错误"),
    ACCOUNT_DISABLED(2004, "账号已被禁用"),
    ADDRESS_NOT_FOUND(2005, "收货地址不存在"),
    ADDRESS_LIMIT(2006, "收货地址数量已达上限"),

    // ---------- 商品段 3000-3099 ----------
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_OFF_SALE(3002, "商品已下架"),
    PRODUCT_STOCK_NOT_ENOUGH(3003, "商品库存不足"),
    CATEGORY_NOT_FOUND(3004, "商品类目不存在"),
    PRODUCT_STATUS_NOT_ALLOWED(3005, "商品状态不允许该操作"),
    PRODUCT_INFO_INCOMPLETE(3006, "商品信息不完整"),

    // ---------- 订单段 4000-4099 ----------
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_NOT_ALLOWED(4002, "订单状态不允许该操作"),
    CART_ITEM_INVALID(4003, "购物车项不存在或已失效"),
    CART_EMPTY(4004, "购物车为空"),
    ORDER_ADDRESS_NOT_FOUND(4005, "收货地址不存在"),
    STOCK_NOT_ENOUGH(4006, "库存不足"),
    PRICE_CHANGED(4007, "商品价格已变化"),
    ORDER_ALREADY_PAID(4008, "订单已支付"),
    SHIPPING_NO_REQUIRED(4009, "运单号必填"),

    // ---------- 售后段 5000-5099 ----------
    REFUND_NOT_FOUND(5001, "售后单不存在"),
    ORDER_STATUS_NOT_REFUNDABLE(5002, "订单状态不允许退款"),
    REFUND_DUPLICATE(5003, "重复退款申请"),
    REFUND_AMOUNT_INVALID(5004, "退款金额非法"),
    REFUND_STATUS_NOT_ALLOWED(5005, "售后单状态不允许该操作"),
    REFUND_REASON_REQUIRED(5006, "退款原因必填"),
    MERCHANT_REPLY_REQUIRED(5007, "商家回复必填"),
    ADMIN_RESULT_REQUIRED(5008, "裁决意见必填"),

    // ---------- 商家段 6000-6099 ----------
    SHOP_NOT_FOUND(6001, "店铺不存在"),
    SHOP_NOT_APPROVED(6002, "店铺未通过审核"),
    SHOP_AUDITING(6003, "店铺审核中"),
    NOT_MY_SHOP_DATA(6004, "非本店数据"),
    SHOP_REJECTED(6005, "店铺已被驳回"),
    AUDIT_REASON_REQUIRED(6006, "审核原因必填"),

    // ---------- 管理员段 7000-7099 ----------
    AUDIT_PARAM_MISSING(7001, "审核参数缺失"),
    AUDIT_REASON_OR_RESULT_REQUIRED(7002, "审核原因或裁决意见必填"),
    AUDIT_TARGET_NOT_FOUND(7003, "审核目标不存在"),
    NO_ADMIN_PERMISSION(7004, "无管理员权限"),
    AUDIT_CONFLICT(7005, "审核操作冲突");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
