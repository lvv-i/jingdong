package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.IdGenerator;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AdminRefundHandleDTO;
import com.example.shop.dto.RefundCreateDTO;
import com.example.shop.entity.AuditLog;
import com.example.shop.entity.Notice;
import com.example.shop.entity.Order;
import com.example.shop.entity.PaymentRecord;
import com.example.shop.entity.RefundRequest;
import com.example.shop.enums.OrderStatus;
import com.example.shop.enums.RefundStatus;
import com.example.shop.mapper.AuditLogMapper;
import com.example.shop.mapper.NoticeMapper;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.mapper.PaymentRecordMapper;
import com.example.shop.mapper.RefundRequestMapper;
import com.example.shop.security.LoginUser;
import com.example.shop.security.UserContext;
import com.example.shop.service.RefundService;
import com.example.shop.vo.RefundListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后单服务实现（T1 售后状态机核心）
 * U-018/U-019/U-020/U-021（用户）；M-012/M-013/M-014（商家仅本店）；A-014/A-015（管理员全局）
 * 流转：REFUNDING→MERCHANT_AGREED/REJECTED/CLOSED；MERCHANT_AGREED→REFUNDED（系统退款）；
 *       MERCHANT_REJECTED→ADMIN_INTERVENED；ADMIN_INTERVENED→REFUNDED/CLOSED
 * 留痕必存：reason / merchant_reply / admin_result；商家处理与管理员裁决写 audit_logs（T4 强制清单）
 */
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    /** 进行中状态集合（T1：非终态均视为进行中，防止重复申请） */
    private static final List<String> IN_PROGRESS_STATUSES = List.of(
            RefundStatus.REFUNDING.name(),
            RefundStatus.MERCHANT_AGREED.name(),
            RefundStatus.MERCHANT_REJECTED.name(),
            RefundStatus.ADMIN_INTERVENED.name());

    private final RefundRequestMapper refundRequestMapper;
    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final AuditLogMapper auditLogMapper;
    private final NoticeMapper noticeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RefundCreateDTO dto) {
        Long userId = UserContext.requireUserId();
        // 4001 订单归属本人
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        // 5002 订单 status∈{PAID,SHIPPED,COMPLETED}
        if (!List.of(OrderStatus.PAID.name(), OrderStatus.SHIPPED.name(), OrderStatus.COMPLETED.name())
                .contains(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_REFUNDABLE);
        }
        // 5003 无进行中售后单
        long inProgress = refundRequestMapper.selectCount(new LambdaQueryWrapper<RefundRequest>()
                .eq(RefundRequest::getOrderId, order.getId())
                .in(RefundRequest::getStatus, IN_PROGRESS_STATUSES));
        if (inProgress > 0) {
            throw new BusinessException(ErrorCode.REFUND_DUPLICATE);
        }
        // 5006 退款原因必填（DTO 已校验，服务层兜底）
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException(ErrorCode.REFUND_REASON_REQUIRED);
        }
        // 5004 金额≤实付且大于 0
        BigDecimal amount = dto.getRefundAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.compareTo(order.getPayAmount()) > 0) {
            throw new BusinessException(ErrorCode.REFUND_AMOUNT_INVALID);
        }
        RefundRequest refund = new RefundRequest();
        refund.setRefundNo(IdGenerator.refundNo());
        refund.setOrderId(order.getId());
        refund.setUserId(userId);
        refund.setMerchantId(order.getMerchantId());
        refund.setRefundAmount(amount);
        refund.setReason(dto.getReason());
        refund.setStatus(RefundStatus.REFUNDING.name());
        refundRequestMapper.insert(refund);
        return refund.getId();
    }

    @Override
    public PageResult<RefundListItemVO> listUser(PageQuery pageQuery, String status) {
        Long userId = UserContext.requireUserId();
        return queryPage(new LambdaQueryWrapper<RefundRequest>()
                        .eq(RefundRequest::getUserId, userId), pageQuery, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        RefundRequest refund = requireRefund(id);
        // 数据权限：仅本人（T4）
        if (!refund.getUserId().equals(UserContext.requireUserId())) {
            throw new BusinessException(ErrorCode.REFUND_NOT_FOUND);
        }
        // 5005 仅 REFUNDING 可撤销（商家未处理时）
        requireStatus(refund, RefundStatus.REFUNDING);
        refund.setStatus(RefundStatus.CLOSED.name());
        refundRequestMapper.updateById(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void intervene(Long id) {
        RefundRequest refund = requireRefund(id);
        if (!refund.getUserId().equals(UserContext.requireUserId())) {
            throw new BusinessException(ErrorCode.REFUND_NOT_FOUND);
        }
        // 5005 仅 MERCHANT_REJECTED 可申请平台介入
        requireStatus(refund, RefundStatus.MERCHANT_REJECTED);
        refund.setStatus(RefundStatus.ADMIN_INTERVENED.name());
        refundRequestMapper.updateById(refund);
        // 通知平台介入处理中（T5 U-021 备注：申请平台介入）
        sendNotice(refund.getUserId(), "平台介入受理",
                "您的退款单 " + refund.getRefundNo() + " 已提交平台介入，请等待裁决结果");
    }

    @Override
    public PageResult<RefundListItemVO> listMerchant(PageQuery pageQuery, String status) {
        Long shopId = requireShopId();
        return queryPage(new LambdaQueryWrapper<RefundRequest>()
                        .eq(RefundRequest::getMerchantId, shopId), pageQuery, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agree(Long id, String reply) {
        RefundRequest refund = requireRefund(id);
        // 6004 非本店售后单
        requireShopOwn(refund, requireShopId());
        // 5007 商家回复必填
        if (!StringUtils.hasText(reply)) {
            throw new BusinessException(ErrorCode.MERCHANT_REPLY_REQUIRED);
        }
        // 5005 仅 REFUNDING 可同意
        requireStatus(refund, RefundStatus.REFUNDING);
        refund.setStatus(RefundStatus.MERCHANT_AGREED.name());
        refund.setMerchantReply(reply);
        refundRequestMapper.updateById(refund);
        writeAuditLog("REFUND", refund.getId(), "AGREE", "商家同意退款：" + reply);
        sendNotice(refund.getUserId(), "退款同意",
                "您的退款单 " + refund.getRefundNo() + " 商家已同意，退款将原路退回");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reply) {
        RefundRequest refund = requireRefund(id);
        requireShopOwn(refund, requireShopId());
        if (!StringUtils.hasText(reply)) {
            throw new BusinessException(ErrorCode.MERCHANT_REPLY_REQUIRED);
        }
        requireStatus(refund, RefundStatus.REFUNDING);
        refund.setStatus(RefundStatus.MERCHANT_REJECTED.name());
        refund.setMerchantReply(reply);
        refundRequestMapper.updateById(refund);
        writeAuditLog("REFUND", refund.getId(), "REJECT", "商家拒绝退款：" + reply);
        sendNotice(refund.getUserId(), "退款被拒绝",
                "您的退款单 " + refund.getRefundNo() + " 商家已拒绝：" + reply + "，可申请平台介入");
    }

    @Override
    public PageResult<RefundListItemVO> listAdmin(PageQuery pageQuery, String status) {
        return queryPage(new LambdaQueryWrapper<RefundRequest>(), pageQuery, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, AdminRefundHandleDTO dto) {
        RefundRequest refund = requireRefund(id);
        // 5005 仅 ADMIN_INTERVENED 可裁决
        requireStatus(refund, RefundStatus.ADMIN_INTERVENED);
        // 5008 裁决意见必填
        if (!StringUtils.hasText(dto.getAdminResult())) {
            throw new BusinessException(ErrorCode.ADMIN_RESULT_REQUIRED);
        }
        boolean agree = Boolean.TRUE.equals(dto.getAgree());
        refund.setAdminResult(dto.getAdminResult());
        refund.setStatus(agree ? RefundStatus.REFUNDED.name() : RefundStatus.CLOSED.name());
        refundRequestMapper.updateById(refund);

        // agree=true 写退款流水（T5 A-015：payment_records(REFUND)）
        if (agree) {
            PaymentRecord record = new PaymentRecord();
            record.setPaymentNo(IdGenerator.paymentNo());
            record.setOrderId(refund.getOrderId());
            record.setUserId(refund.getUserId());
            record.setRefundId(refund.getId());
            record.setAmount(refund.getRefundAmount());
            record.setType("REFUND");
            record.setStatus("SUCCESS");
            paymentRecordMapper.insert(record);
        }
        writeAuditLog("REFUND", refund.getId(), "HANDLE_REFUND",
                (agree ? "同意退款：" : "驳回退款：") + dto.getAdminResult());
        sendNotice(refund.getUserId(), agree ? "退款成功" : "退款关闭",
                "您的退款单 " + refund.getRefundNo() + " 平台裁决：" + dto.getAdminResult());
    }

    // ---------- 私有工具 ----------

    /** 列表查询：status 过滤走 T1 枚举校验 */
    private PageResult<RefundListItemVO> queryPage(LambdaQueryWrapper<RefundRequest> wrapper,
                                                   PageQuery pageQuery, String status) {
        if (StringUtils.hasText(status)) {
            if (!RefundStatus.isValid(status)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "售后状态值非法");
            }
            wrapper.eq(RefundRequest::getStatus, status);
        }
        wrapper.orderByDesc(RefundRequest::getCreatedAt);
        Page<RefundRequest> page = refundRequestMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        List<RefundListItemVO> list = page.getRecords().stream().map(r -> new RefundListItemVO(
                r.getId(), r.getRefundNo(), r.getOrderId(), r.getRefundAmount(), r.getReason(),
                r.getStatus(), r.getMerchantReply(), r.getAdminResult(), r.getCreatedAt())).toList();
        return PageResult.of(list, page.getTotal());
    }

    private RefundRequest requireRefund(Long id) {
        RefundRequest refund = refundRequestMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException(ErrorCode.REFUND_NOT_FOUND);
        }
        return refund;
    }

    /** 状态校验（T1 状态机，非法流转拒绝 5005） */
    private void requireStatus(RefundRequest refund, RefundStatus expected) {
        if (!expected.name().equals(refund.getStatus())) {
            throw new BusinessException(ErrorCode.REFUND_STATUS_NOT_ALLOWED);
        }
    }

    /** 商家数据权限：售后单归属本店（T4 清单 #3；6004） */
    private void requireShopOwn(RefundRequest refund, Long shopId) {
        if (refund.getMerchantId() == null || !refund.getMerchantId().equals(shopId)) {
            throw new BusinessException(ErrorCode.NOT_MY_SHOP_DATA);
        }
    }

    /** 当前商家店铺ID（JWT 签发时携带；异常场景兜底 6001） */
    private Long requireShopId() {
        LoginUser user = UserContext.get();
        Long shopId = user == null ? null : user.getShopId();
        if (shopId == null) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
        return shopId;
    }

    /** 审计日志（T4 强制清单：管理员/商家敏感操作留痕） */
    private void writeAuditLog(String targetType, Long targetId, String action, String remark) {
        LoginUser user = UserContext.get();
        AuditLog log = new AuditLog();
        log.setOperatorId(user == null ? 0L : user.getId());
        log.setOperatorRole(user == null ? "UNKNOWN" : user.getRole());
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setRemark(remark);
        auditLogMapper.insert(log);
    }

    /** 站内通知 */
    private void sendNotice(Long receiverId, String title, String content) {
        Notice notice = new Notice();
        notice.setReceiverId(receiverId);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setReadStatus(0);
        noticeMapper.insert(notice);
    }
}
