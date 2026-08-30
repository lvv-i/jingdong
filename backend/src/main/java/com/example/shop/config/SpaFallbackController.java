package com.example.shop.config;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * SPA 路由回退：Vue Router history 模式下，非 /api/ 非静态资源请求返回对应 index.html
 * 
 * 映射：
 * - /admin/** 非静态 → admin-web/dist/index.html
 * - 其余所有非 /api/ 请求 → frontend-user/dist/index.html
 */
@Controller
public class SpaFallbackController {

    /**
     * 所有非 /api/、非 /admin/ 的非静态请求回退到用户端 index.html
     * 匹配顺序：Spring 先尝试 static resource handler 匹配文件，
     *           不匹配的路径（如 /cart, /product/1）落到此方法
     */
    @GetMapping(value = {
        "/",
        "/index.html",
        "/cart",
        "/checkout",
        "/login",
        "/search",
        "/product/**",
        "/order/**",
        "/refund/**",
        "/notice/**",
        "/mine/**",
        "/profile/**"
    })
    public Resource spaIndex() throws IOException {
        Resource resource = new FileSystemResource("../frontend-user/dist/index.html");
        if (resource.exists()) return resource;
        return null;
    }

    /**
     * /admin/** 路径回退到后台端 index.html
     */
    @GetMapping("/admin/**")
    public Resource adminIndex() throws IOException {
        Resource resource = new FileSystemResource("../admin-web/dist/index.html");
        if (resource.exists()) return resource;
        return null;
    }
}