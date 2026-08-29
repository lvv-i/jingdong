package com.example.shop.service;

import com.example.shop.common.PageResult;
import com.example.shop.dto.CartAddDTO;
import com.example.shop.dto.CartUpdateDTO;
import com.example.shop.vo.CartItemVO;

/**
 * 购物车服务
 * 接口映射：U-008 列表 / U-009 加购 / U-010 修改 / U-011 删除
 */
public interface CartService {

    /** U-008 购物车列表（T5 通用约定：data 为 {list, total}） */
    PageResult<CartItemVO> list();

    /** U-009 加购（仅 ON_SALE 可加购；重复商品累加数量） */
    Long add(CartAddDTO dto);

    /** U-010 修改数量/勾选 */
    void update(Long id, CartUpdateDTO dto);

    /** U-011 软删除 */
    void delete(Long id);
}
