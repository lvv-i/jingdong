package com.example.shop.service;

import com.example.shop.dto.AddressDTO;
import com.example.shop.vo.AddressVO;

import java.util.List;

/**
 * 收货地址服务
 * 接口映射：U-003 列表 / U-004 新增 / U-005 编辑 / U-006 删除 / U-007 设为默认
 * 数据范围：仅本人（T4）；上限 20 个（2006）
 */
public interface AddressService {

    /** U-003 地址列表 */
    List<AddressVO> list();

    /** U-004 新增（超 20 个返回 2006） */
    Long add(AddressDTO dto);

    /** U-005 编辑（校验归属本人 2005） */
    void update(Long id, AddressDTO dto);

    /** U-006 软删除 */
    void delete(Long id);

    /** U-007 设为默认地址 */
    void setDefault(Long id);
}
