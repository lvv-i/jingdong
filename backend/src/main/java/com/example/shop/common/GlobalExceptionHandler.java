package com.example.shop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：统一转换为 ApiResult（HTTP 保持 200，传输层错误除外）
 * - BusinessException → 业务错误码
 * - 参数校验异常（@Valid / 类型不匹配 / 缺参） → 1001 参数错误
 * - 其余未预期异常 → 1005 系统错误（不暴露堆栈，仅记录日志）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（主通道） */
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusiness(BusinessException e) {
        return ApiResult.fail(e.getErrorCode(), e.getMessage());
    }

    /** @RequestBody @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? ErrorCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return ApiResult.fail(ErrorCode.PARAM_ERROR, msg);
    }

    /** 表单绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? ErrorCode.PARAM_ERROR.getMessage() : fieldError.getDefaultMessage();
        return ApiResult.fail(ErrorCode.PARAM_ERROR, msg);
    }

    /** 请求体缺失/格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return ApiResult.fail(ErrorCode.PARAM_ERROR, "请求体缺失或格式错误");
    }

    /** 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ApiResult.fail(ErrorCode.PARAM_ERROR, "缺少参数：" + e.getParameterName());
    }

    /** 参数类型不匹配（路径/查询参数） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResult<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ApiResult.fail(ErrorCode.PARAM_ERROR, "参数类型错误：" + e.getName());
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ApiResult.fail(ErrorCode.PARAM_ERROR, "请求方法不支持");
    }

    /** 静态资源/路径不存在（Spring Boot 3.2 对 404 抛此异常） */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ApiResult<Void> handleNotFound(Exception e) {
        return ApiResult.fail(ErrorCode.RESOURCE_NOT_FOUND, "接口不存在");
    }

    /** 系统兜底：不暴露堆栈 */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleOther(Exception e) {
        log.error("[系统错误] {}", e.getMessage(), e);
        return ApiResult.fail(ErrorCode.SYSTEM_ERROR);
    }
}
