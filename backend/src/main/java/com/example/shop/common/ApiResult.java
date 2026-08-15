package com.example.shop.common;

import lombok.Data;

/**
 * 统一返回信封（T3 错误码分段表 + T5 接口清单通用约定）
 * 所有接口返回 {code, message, data, total}；code=200 为成功；业务错误 HTTP 仍为 200
 */
@Data
public class ApiResult<T> {

    /** 状态码：200 成功；其余见 ErrorCode */
    private int code;

    /** 中文提示信息（面向用户可读） */
    private String message;

    /** 业务数据；分页接口为 {list, total} 结构 */
    private T data;

    /** 总数（仅分页接口使用；无意义场景为 null） */
    private Long total;

    private ApiResult(int code, String message, T data, Long total) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.total = total;
    }

    /** 成功（无数据） */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null, null);
    }

    /** 成功（带数据） */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, null);
    }

    /** 成功（分页数据：data 为列表，total 为总数） */
    public static <T> ApiResult<T> successPage(T list, long total) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), list, total);
    }

    /** 业务失败（错误码枚举） */
    public static <T> ApiResult<T> fail(ErrorCode errorCode) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage(), null, null);
    }

    /** 业务失败（自定义 message，错误码枚举兜底） */
    public static <T> ApiResult<T> fail(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.getCode(), message, null, null);
    }
}
