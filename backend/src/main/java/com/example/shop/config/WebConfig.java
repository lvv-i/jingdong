package com.example.shop.config;

import com.example.shop.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：静态资源托管 + 鉴权拦截器 + CORS
 * 生产模式：后端 8080 同时托管三端前端产物，无需 Vite dev server
 * - /            → frontend-user/dist（用户网页端 SPA）
 * - /admin/**    → admin-web/dist（商家/管理员后台 SPA）
 * - /api/**      → 后端 API（鉴权拦截器生效）
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 静态资源映射：后端 8080 托管前端构建产物
     * 演示时只需启动后端一个进程即可访问全部五端
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用户网页端 SPA（Vue 3 + Vite build）
        registry.addResourceHandler("/**")
                .addResourceLocations("file:../frontend-user/dist/")
                .resourceChain(false);
        // 商家/管理员后台 SPA（Vue 3 + Vite build）
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:../admin-web/dist/")
                .resourceChain(false);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 课程项目演示环境放开跨域；前端 B/C/D 三端（Vue/uni-app）均需访问
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
