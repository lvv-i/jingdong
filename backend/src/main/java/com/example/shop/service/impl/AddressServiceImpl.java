package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.dto.AddressDTO;
import com.example.shop.entity.Address;
import com.example.shop.mapper.AddressMapper;
import com.example.shop.security.UserContext;
import com.example.shop.service.AddressService;
import com.example.shop.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务实现
 * U-003/U-004/U-005/U-006/U-007；数据权限：仅本人（T4 清单）
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Value("${app.address-max-count:20}")
    private int maxCount;

    @Override
    public List<AddressVO> list() {
        Long userId = UserContext.requireUserId();
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault).orderByDesc(Address::getUpdatedAt))
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AddressDTO dto) {
        Long userId = UserContext.requireUserId();
        // 2006 数量上限
        long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        if (count >= maxCount) {
            throw new BusinessException(ErrorCode.ADDRESS_LIMIT);
        }
        boolean makeDefault = dto.getIsDefault() != null && dto.getIsDefault() == 1;
        if (makeDefault || count == 0) {
            clearDefault(userId);
        }
        Address address = new Address();
        address.setUserId(userId);
        fill(address, dto);
        address.setIsDefault((makeDefault || count == 0) ? 1 : 0);
        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AddressDTO dto) {
        Address address = requireOwnAddress(id);
        fill(address, dto);
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault(address.getUserId());
            address.setIsDefault(1);
        }
        addressMapper.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireOwnAddress(id);
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Address address = requireOwnAddress(id);
        clearDefault(address.getUserId());
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }

    /** 校验归属本人（T4 数据权限；2005） */
    private Address requireOwnAddress(Long id) {
        Address address = addressMapper.selectById(id);
        Long userId = UserContext.requireUserId();
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        return address;
    }

    /** 清除用户全部默认标记（保证唯一默认） */
    private void clearDefault(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
    }

    private void fill(Address address, AddressDTO dto) {
        address.setReceiver(dto.getReceiver());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
    }

    private AddressVO toVO(Address a) {
        return new AddressVO(a.getId(), a.getReceiver(), a.getPhone(), a.getProvince(),
                a.getCity(), a.getDistrict(), a.getDetail(), a.getIsDefault());
    }
}
