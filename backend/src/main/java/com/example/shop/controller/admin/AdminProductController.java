package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AuditReasonDTO;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AdminProductVO;
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
 * 管理员商品巡检与审核控制器（A-008~A-012；仅 ADMIN；敏感操作写 audit_logs，T4）
 * A-009 强制下架：ON_SALE → OFF_SALE；A-011 通过：PENDING_ON_SALE → ON_SALE；A-012 驳回：PENDING_ON_SALE → DRAFT
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminService adminService;

    /** A-008 商品巡检列表（全局；status/keyword 过滤；含 merchantName） */
    @GetMapping("/products")
    public ApiResult<PageResult<AdminProductVO>> list(PageQuery pageQuery,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String keyword) {
        return ApiResult.success(adminService.listProducts(pageQuery, status, keyword));
    }

    /** A-009 强制下架（auditReason 必填留痕） */
    @PostMapping("/products/{id}/take-down")
    public ApiResult<Void> takeDown(@PathVariable Long id, @Valid @RequestBody AuditReasonDTO dto) {
        adminService.takeDownProduct(id, dto);
        return ApiResult.success();
    }

    /** A-010 商品审核列表（仅 status=PENDING_ON_SALE） */
    @GetMapping("/product-audits")
    public ApiResult<PageResult<AdminProductVO>> audits(PageQuery pageQuery) {
        return ApiResult.success(adminService.listProductAudits(pageQuery));
    }

    /** A-011 上架通过 */
    @PostMapping("/products/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id, @Valid @RequestBody AuditReasonDTO dto) {
        adminService.approveProduct(id, dto);
        return ApiResult.success();
    }

    /** A-012 上架驳回（退回草稿） */
    @PostMapping("/products/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id, @Valid @RequestBody AuditReasonDTO dto) {
        adminService.rejectProduct(id, dto);
        return ApiResult.success();
    }
}
