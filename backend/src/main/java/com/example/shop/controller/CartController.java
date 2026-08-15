package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.dto.CartAddDTO;
import com.example.shop.dto.CartUpdateDTO;
import com.example.shop.service.CartService;
import com.example.shop.vo.CartItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器（U-008~U-011；数据范围：仅本人，T4）
 */
@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** U-008 购物车列表 */
    @GetMapping
    public ApiResult<List<CartItemVO>> list() {
        return ApiResult.success(cartService.list());
    }

    /** U-009 加购（仅 ON_SALE 可加购；重复商品累加数量） */
    @PostMapping
    public ApiResult<Map<String, Long>> add(@Valid @RequestBody CartAddDTO dto) {
        return ApiResult.success(Map.of("id", cartService.add(dto)));
    }

    /** U-010 修改数量/勾选 */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody CartUpdateDTO dto) {
        cartService.update(id, dto);
        return ApiResult.success();
    }

    /** U-011 软删除 */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return ApiResult.success();
    }
}
