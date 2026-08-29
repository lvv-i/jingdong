package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.PageResult;
import com.example.shop.dto.CartAddDTO;
import com.example.shop.dto.CartUpdateDTO;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.enums.ProductStatus;
import com.example.shop.mapper.CartItemMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.security.UserContext;
import com.example.shop.service.CartService;
import com.example.shop.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务实现
 * U-008/U-009/U-010/U-011；数据权限：仅本人（T4 清单）
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<CartItemVO> list() {
        Long userId = UserContext.requireUserId();
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdatedAt));
        List<CartItemVO> list = items.stream().map(item -> {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                return new CartItemVO(item.getId(), item.getProductId(), "商品已失效",
                        null, item.getQuantity(), item.getSelected(), 0);
            }
            return new CartItemVO(item.getId(), item.getProductId(), product.getTitle(),
                    product.getPrice(), item.getQuantity(), item.getSelected(), product.getStock());
        }).toList();
        return PageResult.of(list, list.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CartAddDTO dto) {
        Long userId = UserContext.requireUserId();
        Product product = productMapper.selectById(dto.getProductId());
        // 3001 商品不存在；3002 非 ON_SALE 不可加购
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!ProductStatus.ON_SALE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        }
        // 重复商品累加数量（T5 U-009 备注）
        CartItem exist = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, dto.getProductId()));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + dto.getQuantity());
            exist.setSelected(1);
            cartItemMapper.updateById(exist);
            return exist.getId();
        }
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setSelected(1);
        cartItemMapper.insert(item);
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CartUpdateDTO dto) {
        CartItem item = requireOwnItem(id);
        if (dto.getQuantity() != null) {
            item.setQuantity(dto.getQuantity());
        }
        if (dto.getSelected() != null) {
            item.setSelected(dto.getSelected());
        }
        cartItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireOwnItem(id);
        cartItemMapper.deleteById(id);
    }

    /** 校验条目归属本人（T4 数据权限；4003 不存在或已失效） */
    private CartItem requireOwnItem(Long id) {
        CartItem item = cartItemMapper.selectById(id);
        Long userId = UserContext.requireUserId();
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_INVALID);
        }
        return item;
    }
}
