package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.UserStatusDTO;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AdminUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户治理控制器（A-016/A-017；仅 ADMIN）
 * A-017 禁用后 token 立即失效（拦截器查库校验，T5 备注）
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    /** A-016 用户列表（keyword/role 过滤） */
    @GetMapping
    public ApiResult<PageResult<AdminUserVO>> list(PageQuery pageQuery,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String role) {
        return ApiResult.success(adminService.listUsers(pageQuery, keyword, role));
    }

    /** A-017 修改用户状态：NORMAL/DISABLED */
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        adminService.updateUserStatus(id, dto);
        return ApiResult.success();
    }
}
