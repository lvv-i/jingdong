package com.example.shop.controller;

import com.example.shop.common.ApiResult;
import com.example.shop.dto.AddressDTO;
import com.example.shop.service.AddressService;
import com.example.shop.vo.AddressVO;
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
 * 收货地址控制器（U-003~U-007；数据范围：仅本人，T4）
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /** U-003 地址列表 */
    @GetMapping
    public ApiResult<List<AddressVO>> list() {
        return ApiResult.success(addressService.list());
    }

    /** U-004 新增（超 20 个返回 2006） */
    @PostMapping
    public ApiResult<Map<String, Long>> add(@Valid @RequestBody AddressDTO dto) {
        return ApiResult.success(Map.of("id", addressService.add(dto)));
    }

    /** U-005 编辑（校验归属本人 2005） */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        addressService.update(id, dto);
        return ApiResult.success();
    }

    /** U-006 软删除 */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ApiResult.success();
    }

    /** U-007 设为默认地址 */
    @PutMapping("/{id}/default")
    public ApiResult<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return ApiResult.success();
    }
}
