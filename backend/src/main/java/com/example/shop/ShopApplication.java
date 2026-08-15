package com.example.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 京东风格电商平台后端启动类
 *
 * 包结构约定（.qoder/members/member-a/rules/backend-layering.md）：
 * common/config/security/controller/merchant/admin/service/mapper/entity/dto/vo/enums
 */
@SpringBootApplication
@EnableScheduling
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
