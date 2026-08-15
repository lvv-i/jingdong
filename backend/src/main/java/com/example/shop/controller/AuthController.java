package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.dto.LoginDTO;
import com.example.shop.dto.ProfileUpdateDTO;
import com.example.shop.dto.RegisterDTO;
import com.example.shop.dto.SmsCodeDTO;
import com.example.shop.dto.SmsLoginDTO;
import com.example.shop.service.AuthService;
import com.example.shop.vo.LoginVO;
import com.example.shop.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证与个人中心控制器
 * P-001 注册 / P-002 密码登录 / P-007 短信验证码 / P-008 验证码登录（白名单）
 * U-001 个人资料 / U-002 修改资料（登录用户）
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** P-001 注册（角色默认 USER） */
    @PostMapping("/register")
    public ApiResult<Map<String, Long>> register(@Valid @RequestBody RegisterDTO dto) {
        return ApiResult.success(Map.of("userId", authService.register(dto)));
    }

    /** P-002 账号密码登录（商家签发 shopId） */
    @PostMapping("/login")
    public ApiResult<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResult.success(authService.login(dto));
    }

    /** P-007 发送短信验证码（demo 固定码 123456，60 秒限频） */
    @PostMapping("/sms-code")
    public ApiResult<Void> sendSmsCode(@Valid @RequestBody SmsCodeDTO dto) {
        authService.sendSmsCode(dto.getPhone());
        return ApiResult.success();
    }

    /** P-008 短信验证码登录（未注册手机号自动注册） */
    @PostMapping("/login/sms")
    public ApiResult<LoginVO> smsLogin(@Valid @RequestBody SmsLoginDTO dto) {
        return ApiResult.success(authService.smsLogin(dto));
    }

    /** U-001 当前用户资料 */
    @GetMapping("/profile")
    public ApiResult<UserProfileVO> profile() {
        return ApiResult.success(authService.profile());
    }

    /** U-002 修改资料（改密需原密码校验） */
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        authService.updateProfile(dto);
        return ApiResult.success();
    }
}
