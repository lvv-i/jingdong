package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AuditReasonDTO;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AdminMerchantVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员商家审核控制器（A-001~A-003；仅 ADMIN；敏感操作写 audit_logs，T4）
 * A-002 通过：PENDING_AUDIT → APPROVED；A-003 驳回：PENDING_AUDIT → REJECTED
 */
@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminService adminService;

    /** A-001 商家列表（auditStatus 过滤；含 userPhone） */
    @GetMapping
    public ApiResult<PageResult<AdminMerchantVO>> list(PageQuery pageQuery,
                                                       @RequestParam(required = false) String auditStatus) {
        return ApiResult.success(adminService.listMerchants(pageQuery, auditStatus));
    }

    /** A-002 通过入驻 */
    @PostMapping("/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id, @Valid @RequestBody AuditReasonDTO dto) {
        adminService.approveMerchant(id, dto);
        return ApiResult.success();
    }

    /** A-003 驳回入驻 */
    @PostMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id, @Valid @RequestBody AuditReasonDTO dto) {
        adminService.rejectMerchant(id, dto);
        return ApiResult.success();
    }
}
