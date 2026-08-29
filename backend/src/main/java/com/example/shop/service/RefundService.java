package com.example.shop.service;

import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AdminRefundHandleDTO;
import com.example.shop.dto.RefundCreateDTO;
import com.example.shop.vo.RefundListItemVO;

/**
 * 售后单服务（T1 售后状态机核心）
 * 用户侧：U-018 发起 / U-019 列表 / U-020 撤销 / U-021 申请平台介入
 * 商家侧：M-012 列表 / M-013 同意 / M-014 拒绝
 * 管理员侧：A-014 列表 / A-015 裁决
 * 流转：REFUNDING→MERCHANT_AGREED/REJECTED/CLOSED；MERCHANT_AGREED→REFUNDED（系统退款）；
 *       MERCHANT_REJECTED→ADMIN_INTERVENED；ADMIN_INTERVENED→REFUNDED/CLOSED
 */
public interface RefundService {

    /** U-018 发起退款（订单 status∈{PAID,SHIPPED,COMPLETED}；无进行中售后单；金额≤实付）
     * @return Map {refundId, refundNo, status}（T5 v1.1 修正） */
    java.util.Map<String, Object> create(RefundCreateDTO dto);

    /** U-019 用户售后列表（仅本人） */
    PageResult<RefundListItemVO> listUser(PageQuery pageQuery, String status);

    /** U-020 撤销退款：REFUNDING → CLOSED（商家未处理时） */
    void cancel(Long id);

    /** U-021 申请平台介入：MERCHANT_REJECTED → ADMIN_INTERVENED */
    void intervene(Long id);

    /** M-012 商家售后列表（仅本店） */
    PageResult<RefundListItemVO> listMerchant(PageQuery pageQuery, String status);

    /** M-013 商家同意：REFUNDING → MERCHANT_AGREED；留痕 merchant_reply；写 audit_logs(AGREE) */
    void agree(Long id, String reply);

    /** M-014 商家拒绝：REFUNDING → MERCHANT_REJECTED；写 audit_logs(REJECT) */
    void reject(Long id, String reply);

    /** A-014 管理员售后列表（全局） */
    PageResult<RefundListItemVO> listAdmin(PageQuery pageQuery, String status);

    /** A-015 管理员裁决：ADMIN_INTERVENED → REFUNDED/CLOSED；留痕 admin_result；agree=true 写退款流水 */
    void handle(Long id, AdminRefundHandleDTO dto);
}
