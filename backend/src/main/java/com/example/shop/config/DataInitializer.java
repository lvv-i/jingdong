package com.example.shop.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动数据兜底：种子账号密码哈希校正
 *
 * 背景：backend/sql/20260812_002_seed.sql 中 password_hash 为占位值（SQL 无法预生成 BCrypt），
 * 本类启动时校验每个账号：若哈希无法匹配「密码=账号名」，则重置为 BCrypt(username)。
 * 依据：docs/phase1/member-d/deliverables/D-02-测试账号与演示数据方案.md
 *       （user001/user002/merchant001/merchant002/admin001，密码=账号名）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .isNotNull(User::getUsername));
        int fixed = 0;
        for (User user : users) {
            // 演示约定：密码 = 账号名；不匹配则重置（幂等：匹配成功不重复写库）
            if (!passwordEncoder.matches(user.getUsername(), user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(user.getUsername()));
                userMapper.updateById(user);
                fixed++;
                log.info("[DataInitializer] 重置账号 {} 的密码哈希为 BCrypt(username)", user.getUsername());
            }
        }
        log.info("[DataInitializer] 种子账号哈希校验完成：共 {} 个账号，重置 {} 个", users.size(), fixed);
    }
}
