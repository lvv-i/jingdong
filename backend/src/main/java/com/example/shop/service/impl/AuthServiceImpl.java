package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.dto.LoginDTO;
import com.example.shop.dto.ProfileUpdateDTO;
import com.example.shop.dto.RegisterDTO;
import com.example.shop.dto.SmsLoginDTO;
import com.example.shop.entity.MerchantShop;
import com.example.shop.entity.User;
import com.example.shop.mapper.MerchantShopMapper;
import com.example.shop.mapper.UserMapper;
import com.example.shop.security.JwtUtil;
import com.example.shop.security.LoginUser;
import com.example.shop.security.UserContext;
import com.example.shop.service.AuthService;
import com.example.shop.vo.LoginVO;
import com.example.shop.vo.UserInfoVO;
import com.example.shop.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证与用户资料服务实现
 * P-001/P-002/P-007/P-008/U-001/U-002
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final MerchantShopMapper merchantShopMapper;
    private final JwtUtil jwtUtil;
    private final SmsCodeService smsCodeService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO dto) {
        // 2001 用户名已存在 / 2002 手机号已注册
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())) > 0) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(LoginUser.ROLE_USER);
        user.setStatus("NORMAL");
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        // 2003 用户名或密码错误（不区分具体原因，防账号枚举）
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        // 2004 账号已被禁用
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        return buildLoginVO(user);
    }

    @Override
    public void sendSmsCode(String phone) {
        smsCodeService.sendCode(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO smsLogin(SmsLoginDTO dto) {
        // demo：校验固定码 123456（一码一用）
        smsCodeService.verifyCode(dto.getPhone(), dto.getSmsCode());
        // 未注册手机号自动注册（T5 P-008 备注）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (user == null) {
            user = new User();
            user.setUsername("user_" + dto.getPhone().substring(7));
            user.setPasswordHash(passwordEncoder.encode(dto.getPhone()));
            user.setPhone(dto.getPhone());
            user.setRole(LoginUser.ROLE_USER);
            user.setStatus("NORMAL");
            userMapper.insert(user);
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        return buildLoginVO(user);
    }

    @Override
    public UserProfileVO profile() {
        User user = currentUser();
        return new UserProfileVO(user.getId(), user.getUsername(), user.getPhone(), user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(ProfileUpdateDTO dto) {
        User user = currentUser();

        // 改密：原密码校验（T5 U-002 备注）
        if (StringUtils.hasText(dto.getNewPassword())) {
            if (!StringUtils.hasText(dto.getOldPassword())
                    || !passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
                throw new BusinessException(ErrorCode.LOGIN_FAILED, "原密码错误");
            }
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        // 改手机号：2002 手机号已注册
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(user.getPhone())) {
            if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())) > 0) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
            user.setPhone(dto.getPhone());
        }
        userMapper.updateById(user);
    }

    /** 组装登录响应；商家登录签发 shopId（T4：shopId 仅商家携带；无店铺返回 6001） */
    private LoginVO buildLoginVO(User user) {
        Long shopId = null;
        if (LoginUser.ROLE_MERCHANT.equals(user.getRole())) {
            MerchantShop shop = merchantShopMapper.selectOne(
                    new LambdaQueryWrapper<MerchantShop>().eq(MerchantShop::getUserId, user.getId()));
            if (shop == null) {
                throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
            }
            shopId = shop.getId();
        }
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole(), shopId);
        String token = jwtUtil.generateToken(loginUser);
        UserInfoVO info = new UserInfoVO(user.getId(), user.getUsername(), user.getRole(), shopId);
        return new LoginVO(token, info);
    }

    private User currentUser() {
        Long userId = UserContext.requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }
}
