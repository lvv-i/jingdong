package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.dto.AuditReasonDTO;
import com.example.shop.dto.UserStatusDTO;
import com.example.shop.entity.AuditLog;
import com.example.shop.entity.MerchantShop;
import com.example.shop.entity.Order;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.enums.MerchantAuditStatus;
import com.example.shop.enums.OrderStatus;
import com.example.shop.enums.ProductStatus;
import com.example.shop.mapper.AuditLogMapper;
import com.example.shop.mapper.MerchantShopMapper;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.mapper.UserMapper;
import com.example.shop.security.LoginUser;
import com.example.shop.security.UserContext;
import com.example.shop.service.AdminService;
import com.example.shop.vo.AdminMerchantVO;
import com.example.shop.vo.AdminOrderVO;
import com.example.shop.vo.AdminProductVO;
import com.example.shop.vo.AdminUserVO;
import com.example.shop.vo.AuditLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理员服务实现
 * A-001~A-003 / A-008~A-013 / A-016~A-018；全局可见；敏感操作写 audit_logs（T4 强制清单）
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final List<String> VALID_ROLES = List.of(
            LoginUser.ROLE_USER, LoginUser.ROLE_MERCHANT, LoginUser.ROLE_ADMIN);
    private static final List<String> VALID_TARGET_TYPES = List.of("ORDER", "REFUND", "MERCHANT", "PRODUCT");

    private final MerchantShopMapper merchantShopMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;

    @Override
    public PageResult<AdminMerchantVO> listMerchants(PageQuery pageQuery, String auditStatus) {
        LambdaQueryWrapper<MerchantShop> wrapper = new LambdaQueryWrapper<MerchantShop>()
                .orderByDesc(MerchantShop::getCreatedAt);
        if (StringUtils.hasText(auditStatus)) {
            if (!MerchantAuditStatus.isValid(auditStatus)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "审核状态值非法");
            }
            wrapper.eq(MerchantShop::getAuditStatus, auditStatus);
        }
        Page<MerchantShop> page = merchantShopMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);

        // 商家账号手机号批量查
        List<Long> userIds = page.getRecords().stream().map(MerchantShop::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<AdminMerchantVO> list = page.getRecords().stream().map(s -> {
            User u = userMap.get(s.getUserId());
            return new AdminMerchantVO(s.getId(), s.getShopName(), s.getAuditStatus(),
                    s.getAuditReason(), u == null ? null : u.getPhone());
        }).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveMerchant(Long id, AuditReasonDTO dto) {
        MerchantShop shop = requireShop(id);
        // 7002 审核原因必填（DTO 已校验，服务层兜底）
        requireAuditReason(dto);
        // 7005 仅待审核可操作（T1：PENDING_AUDIT → APPROVED）
        if (!MerchantAuditStatus.PENDING_AUDIT.name().equals(shop.getAuditStatus())) {
            throw new BusinessException(ErrorCode.AUDIT_CONFLICT);
        }
        shop.setAuditStatus(MerchantAuditStatus.APPROVED.name());
        shop.setAuditReason(dto.getAuditReason());
        merchantShopMapper.updateById(shop);
        writeAuditLog("MERCHANT", shop.getId(), "APPROVE", dto.getAuditReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectMerchant(Long id, AuditReasonDTO dto) {
        MerchantShop shop = requireShop(id);
        requireAuditReason(dto);
        if (!MerchantAuditStatus.PENDING_AUDIT.name().equals(shop.getAuditStatus())) {
            throw new BusinessException(ErrorCode.AUDIT_CONFLICT);
        }
        shop.setAuditStatus(MerchantAuditStatus.REJECTED.name());
        shop.setAuditReason(dto.getAuditReason());
        merchantShopMapper.updateById(shop);
        writeAuditLog("MERCHANT", shop.getId(), "REJECT", dto.getAuditReason());
    }

    @Override
    public PageResult<AdminProductVO> listProducts(PageQuery pageQuery, String status, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt);
        if (StringUtils.hasText(status)) {
            if (!ProductStatus.isValid(status)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "商品状态值非法");
            }
            wrapper.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getTitle, keyword).or().like(Product::getSubTitle, keyword));
        }
        Page<Product> page = productMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        return toProductPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void takeDownProduct(Long id, AuditReasonDTO dto) {
        Product product = requireProduct(id);
        requireAuditReason(dto);
        // 3005 仅 ON_SALE 可强制下架（T1：ON_SALE → OFF_SALE）
        if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_ALLOWED);
        }
        product.setStatus(ProductStatus.OFF_SALE.name());
        productMapper.updateById(product);
        writeAuditLog("PRODUCT", product.getId(), "TAKE_DOWN", dto.getAuditReason());
    }

    @Override
    public PageResult<AdminProductVO> listProductAudits(PageQuery pageQuery) {
        Page<Product> page = productMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, ProductStatus.PENDING_ON_SALE.name())
                        .orderByAsc(Product::getCreatedAt));
        return toProductPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveProduct(Long id, AuditReasonDTO dto) {
        Product product = requireProduct(id);
        requireAuditReason(dto);
        // 3005 仅 PENDING_ON_SALE 可审核（T1：PENDING_ON_SALE → ON_SALE）
        if (!ProductStatus.PENDING_ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_ALLOWED);
        }
        product.setStatus(ProductStatus.ON_SALE.name());
        productMapper.updateById(product);
        writeAuditLog("PRODUCT", product.getId(), "APPROVE", dto.getAuditReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectProduct(Long id, AuditReasonDTO dto) {
        Product product = requireProduct(id);
        requireAuditReason(dto);
        // 3005 仅 PENDING_ON_SALE 可驳回（T1：PENDING_ON_SALE → DRAFT）
        if (!ProductStatus.PENDING_ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_STATUS_NOT_ALLOWED);
        }
        product.setStatus(ProductStatus.DRAFT.name());
        productMapper.updateById(product);
        writeAuditLog("PRODUCT", product.getId(), "REJECT", dto.getAuditReason());
    }

    @Override
    public PageResult<AdminOrderVO> listOrders(PageQuery pageQuery, String status, String orderNo, Long merchantId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt);
        if (StringUtils.hasText(status)) {
            if (!OrderStatus.isValid(status)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态值非法");
            }
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (merchantId != null) {
            wrapper.eq(Order::getMerchantId, merchantId);
        }
        Page<Order> page = orderMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);

        // 买家/店铺批量查
        List<Long> userIds = page.getRecords().stream().map(Order::getUserId).distinct().toList();
        List<Long> shopIds = page.getRecords().stream().map(Order::getMerchantId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, MerchantShop> shopMap = shopIds.isEmpty() ? Map.of()
                : merchantShopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(MerchantShop::getId, Function.identity()));

        List<AdminOrderVO> list = page.getRecords().stream().map(o -> {
            User buyer = userMap.get(o.getUserId());
            MerchantShop shop = shopMap.get(o.getMerchantId());
            return new AdminOrderVO(o.getId(), o.getOrderNo(),
                    buyer == null ? null : buyer.getUsername(),
                    shop == null ? null : shop.getShopName(),
                    o.getPayAmount(), o.getStatus(), o.getCreatedAt());
        }).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    public PageResult<AdminUserVO> listUsers(PageQuery pageQuery, String keyword, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getPhone, keyword));
        }
        if (StringUtils.hasText(role)) {
            if (!VALID_ROLES.contains(role)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "角色值非法");
            }
            wrapper.eq(User::getRole, role);
        }
        Page<User> page = userMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        List<AdminUserVO> list = page.getRecords().stream().map(u -> new AdminUserVO(
                u.getId(), u.getUsername(), u.getPhone(), u.getRole(), u.getStatus())).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, UserStatusDTO dto) {
        User user = userMapper.selectById(id);
        // 1004 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!dto.getStatus().equals(user.getStatus())) {
            user.setStatus(dto.getStatus());
            userMapper.updateById(user);
        }
    }

    @Override
    public PageResult<AuditLogVO> listLogs(PageQuery pageQuery, String operatorRole, String targetType) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<AuditLog>()
                .orderByDesc(AuditLog::getCreatedAt);
        if (StringUtils.hasText(operatorRole)) {
            if (!VALID_ROLES.contains(operatorRole)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "操作者角色值非法");
            }
            wrapper.eq(AuditLog::getOperatorRole, operatorRole);
        }
        if (StringUtils.hasText(targetType)) {
            if (!VALID_TARGET_TYPES.contains(targetType)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "目标类型值非法");
            }
            wrapper.eq(AuditLog::getTargetType, targetType);
        }
        Page<AuditLog> page = auditLogMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        List<AuditLogVO> list = page.getRecords().stream().map(l -> new AuditLogVO(
                l.getId(), l.getOperatorId(), l.getOperatorRole(), l.getTargetType(),
                l.getTargetId(), l.getAction(), l.getRemark(), l.getCreatedAt())).toList();
        return PageResult.of(list, page.getTotal());
    }

    // ---------- 私有工具 ----------

    private PageResult<AdminProductVO> toProductPage(Page<Product> page) {
        List<Long> shopIds = page.getRecords().stream().map(Product::getMerchantId).distinct().toList();
        Map<Long, MerchantShop> shopMap = shopIds.isEmpty() ? Map.of()
                : merchantShopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(MerchantShop::getId, Function.identity()));
        List<AdminProductVO> list = page.getRecords().stream().map(p -> {
            MerchantShop shop = shopMap.get(p.getMerchantId());
            return new AdminProductVO(p.getId(), p.getTitle(), p.getPrice(), p.getStock(),
                    p.getStatus(), shop == null ? null : shop.getShopName(), p.getCreatedAt());
        }).toList();
        return PageResult.of(list, page.getTotal());
    }

    private MerchantShop requireShop(Long id) {
        MerchantShop shop = merchantShopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
        return shop;
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    /** 审核意见必填（7002 兜底；DTO @NotBlank 先行校验 → 1001） */
    private void requireAuditReason(AuditReasonDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getAuditReason())) {
            throw new BusinessException(ErrorCode.AUDIT_REASON_OR_RESULT_REQUIRED);
        }
    }

    /** 审计日志（T4 强制清单：管理员敏感操作留痕） */
    private void writeAuditLog(String targetType, Long targetId, String action, String remark) {
        LoginUser user = UserContext.get();
        AuditLog log = new AuditLog();
        log.setOperatorId(user == null ? 0L : user.getId());
        log.setOperatorRole(user == null ? LoginUser.ROLE_ADMIN : user.getRole());
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setRemark(remark);
        auditLogMapper.insert(log);
    }
}
