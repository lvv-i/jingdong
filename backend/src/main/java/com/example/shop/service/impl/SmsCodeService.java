package com.example.shop.service.impl;

import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务（T5 P-007/P-008：demo 模式固定码 123456）
 * - 60 秒限频：同一手机号 60 秒内重复发送返回 1007 操作过于频繁
 * - 验证码有效期 300 秒（app.sms-code-ttl-seconds）
 * - 内存存储（课程项目演示范围，重启即失效）
 */
@Slf4j
@Service
public class SmsCodeService {

    @Value("${app.sms-code-fixed:123456}")
    private String fixedCode;

    @Value("${app.sms-code-ttl-seconds:300}")
    private long ttlSeconds;

    /** 限频窗口（秒） */
    private static final long LIMIT_SECONDS = 60;

    /** 校验通过后清除，防止验证码重复使用（一码一用） */
    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    /** 发送验证码（demo：固定码，不真实发送短信，仅记录日志） */
    public void sendCode(String phone) {
        CodeEntry entry = store.get(phone);
        long now = System.currentTimeMillis();
        if (entry != null && (now - entry.sendAtMillis) < LIMIT_SECONDS * 1000L) {
            throw new BusinessException(ErrorCode.TOO_FREQUENT, "验证码发送过于频繁，请 60 秒后再试");
        }
        store.put(phone, new CodeEntry(fixedCode, now));
        log.info("[SmsCode] 向 {} 发送验证码（demo 固定码）：{}", phone, fixedCode);
    }

    /** 校验验证码；通过后立即失效（一码一用） */
    public void verifyCode(String phone, String code) {
        CodeEntry entry = store.get(phone);
        if (entry == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "验证码错误或已过期");
        }
        long now = System.currentTimeMillis();
        if (now - entry.sendAtMillis > ttlSeconds * 1000L) {
            store.remove(phone);
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "验证码错误");
        }
        store.remove(phone);
    }

    private record CodeEntry(String code, long sendAtMillis) {
    }
}
