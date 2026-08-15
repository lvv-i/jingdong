package com.example.shop.security;

import com.example.shop.common.ApiResult;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * JWT 鉴权拦截器（T4 拦截矩阵，后端强制）
 *
 * | 路径                  | 放行角色        | 校验内容 |
 * | /api/merchant/**      | 仅 MERCHANT     | 角色 + shopId 存在（token 签发时保证）+ 资源归属当前店铺 |
 * | /api/admin/**         | 仅 ADMIN        | 角色 |
 * | 其余 /api/**          | 已登录（三角色）| token 有效；用户资源归属当前用户 |
 * | 白名单（见 WHITE_LIST）| 匿名            | 仅限 GET 公开资源与认证接口 |
 *
 * 白名单（T5 接口清单公共组 8 个接口 + CORS 预检 OPTIONS）：
 * P-001 注册 / P-002 密码登录 / P-007 短信验证码 / P-008 验证码登录 / P-003 类目 / P-004~P-006 商品浏览与评价
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    /** 白名单：方法 + 精确路径 */
    private static final Map<String, Set<String>> WHITE_LIST = Map.of(
            "POST", Set.of("/api/users/register", "/api/users/login", "/api/users/sms-code", "/api/users/login/sms"),
            "GET", Set.of("/api/categories")
    );

    /** 白名单前缀（GET，公开商品浏览/评价，路径含 {id} 动态段） */
    private static final Set<String> WHITE_PREFIX_GET = Set.of("/api/products");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 预检请求直接放行（WebConfig 已配置 CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // 1. 白名单：精确匹配
        Set<String> methods = WHITE_LIST.get(method);
        if (methods != null && methods.contains(path)) {
            return true;
        }
        // 2. 白名单：GET /api/products 前缀（列表/详情/评价列表，公开浏览）
        if ("GET".equalsIgnoreCase(method) && WHITE_PREFIX_GET.stream().anyMatch(path::startsWith)) {
            return true;
        }

        // 3. 解析 token（无效/过期 → 1002 未登录）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(response, ErrorCode.NOT_LOGIN);
        }
        LoginUser loginUser;
        try {
            loginUser = jwtUtil.parseToken(authHeader.substring(7));
        } catch (BusinessException e) {
            return reject(response, e.getErrorCode(), e.getMessage());
        }

        // 3.5 账号状态查库校验（T5 A-017 备注：禁用后 token 立即失效）
        User dbUser = userMapper.selectById(loginUser.getId());
        if (dbUser == null || "DISABLED".equals(dbUser.getStatus())) {
            return reject(response, ErrorCode.ACCOUNT_DISABLED);
        }

        // 4. 角色校验（拦截矩阵）
        if (path.startsWith("/api/merchant/")) {
            if (!LoginUser.ROLE_MERCHANT.equals(loginUser.getRole())) {
                return reject(response, ErrorCode.NO_PERMISSION);
            }
            if (loginUser.getShopId() == null) {
                return reject(response, ErrorCode.SHOP_NOT_FOUND);
            }
        } else if (path.startsWith("/api/admin/")) {
            if (!LoginUser.ROLE_ADMIN.equals(loginUser.getRole())) {
                return reject(response, ErrorCode.NO_PERMISSION);
            }
        }
        // 其余 /api/**：已登录即可；数据范围校验（归属本人/本店）由各 Service 按 T4 数据权限清单强制

        UserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 鉴权失败统一 JSON 输出（拦截器内不走 GlobalExceptionHandler，手动序列化） */
    private boolean reject(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        return reject(response, errorCode, errorCode.getMessage());
    }

    private boolean reject(HttpServletResponse response, ErrorCode errorCode, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResult<Void> result = ApiResult.fail(errorCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}
