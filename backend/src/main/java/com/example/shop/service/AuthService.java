package com.example.shop.service;

import com.example.shop.dto.LoginDTO;
import com.example.shop.dto.ProfileUpdateDTO;
import com.example.shop.dto.RegisterDTO;
import com.example.shop.dto.SmsLoginDTO;
import com.example.shop.vo.LoginVO;
import com.example.shop.vo.UserProfileVO;

/**
 * 认证与用户资料服务
 * 接口映射：P-001 注册 / P-002 密码登录 / P-007 短信验证码 / P-008 验证码登录 / U-001 资料 / U-002 修改资料
 */
public interface AuthService {

    /** P-001 注册（角色默认 USER；商家/管理员由种子数据） */
    Long register(RegisterDTO dto);

    /** P-002 账号密码登录（商家登录签发 shopId；无店铺返回 6001） */
    LoginVO login(LoginDTO dto);

    /** P-007 发送短信验证码（demo 固定码 123456，60 秒限频） */
    void sendSmsCode(String phone);

    /** P-008 短信验证码登录（未注册手机号自动注册） */
    LoginVO smsLogin(SmsLoginDTO dto);

    /** U-001 当前用户资料 */
    UserProfileVO profile();

    /** U-002 修改资料（改密需原密码校验） */
    void updateProfile(ProfileUpdateDTO dto);
}
