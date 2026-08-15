package com.example.shop.common;

import lombok.Getter;

/**
 * 业务异常：业务错误统一抛 BusinessException(code, message)
 * 由 GlobalExceptionHandler 统一转换为 ApiResult；禁止向调用方暴露堆栈
 * 使用：throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_ALLOWED)
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码枚举 */
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
