package com.example.shop.service;

import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AuditReasonDTO;
import com.example.shop.dto.UserStatusDTO;
import com.example.shop.vo.AdminMerchantVO;
import com.example.shop.vo.AdminOrderVO;
import com.example.shop.vo.AdminProductVO;
import com.example.shop.vo.AdminUserVO;
import com.example.shop.vo.AuditLogVO;

/**
 * 管理员服务（全局可见；敏感操作写 audit_logs，T4 强制清单）
 * 接口映射：A-001~A-003（商家审核）/ A-008~A-013（商品巡检审核/订单）/ A-016~A-019（用户/日志/统计）
 * 注：类目管理 A-004~A-007 见 CategoryService；售后列表与裁决 A-014/A-015 见 RefundService；统计 A-019 见 StatsService
 */
public interface AdminService {

    /** A-001 商家列表（auditStatus 过滤；含 userPhone） */
    PageResult<AdminMerchantVO> listMerchants(PageQuery pageQuery, String auditStatus);

    /** A-002 通过入驻：PENDING_AUDIT → APPROVED；写 audit_logs(MERCHANT/APPROVE) */
    void approveMerchant(Long id, AuditReasonDTO dto);

    /** A-003 驳回入驻：PENDING_AUDIT → REJECTED；写 audit_logs(MERCHANT/REJECT) */
    void rejectMerchant(Long id, AuditReasonDTO dto);

    /** A-008 商品巡检列表（全局；status/keyword 过滤；含 merchantName） */
    PageResult<AdminProductVO> listProducts(PageQuery pageQuery, String status, String keyword);

    /** A-009 强制下架：ON_SALE → OFF_SALE；写 audit_logs(PRODUCT/TAKE_DOWN) */
    void takeDownProduct(Long id, AuditReasonDTO dto);

    /** A-010 商品审核列表（仅 PENDING_ON_SALE） */
    PageResult<AdminProductVO> listProductAudits(PageQuery pageQuery);

    /** A-011 上架通过：PENDING_ON_SALE → ON_SALE；写 audit_logs(PRODUCT/APPROVE) */
    void approveProduct(Long id, AuditReasonDTO dto);

    /** A-012 上架驳回：PENDING_ON_SALE → DRAFT；写 audit_logs(PRODUCT/REJECT) */
    void rejectProduct(Long id, AuditReasonDTO dto);

    /** A-013 全局订单列表（status/orderNo/merchantId 过滤；含 userName/merchantName） */
    PageResult<AdminOrderVO> listOrders(PageQuery pageQuery, String status, String orderNo, Long merchantId);

    /** A-016 用户列表（keyword/role 过滤） */
    PageResult<AdminUserVO> listUsers(PageQuery pageQuery, String keyword, String role);

    /** A-017 修改用户状态：NORMAL/DISABLED（1004 用户不存在） */
    void updateUserStatus(Long id, UserStatusDTO dto);

    /** A-018 审计日志列表（operatorRole/targetType 过滤） */
    PageResult<AuditLogVO> listLogs(PageQuery pageQuery, String operatorRole, String targetType);
}
