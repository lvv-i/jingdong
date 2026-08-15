package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.dto.CategoryCreateDTO;
import com.example.shop.dto.CategoryUpdateDTO;
import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.mapper.CategoryMapper;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.service.CategoryService;
import com.example.shop.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 类目服务实现
 * P-003/A-004/A-005/A-006/A-007
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryVO> listEnabled() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, "ENABLED")
                        .orderByAsc(Category::getSortOrder).orderByAsc(Category::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<CategoryVO> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder).orderByAsc(Category::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public Long create(CategoryCreateDTO dto) {
        // 父类目校验（3004）
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        if (parentId != 0 && categoryMapper.selectById(parentId) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        category.setStatus("ENABLED");
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryUpdateDTO dto) {
        Category category = requireCategory(id);
        if (StringUtils.hasText(dto.getName())) {
            category.setName(dto.getName());
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            category.setStatus(dto.getStatus());
        }
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireCategory(id);
        // 有商品时拒绝（T5 A-007 备注）
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (productCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该类目下存在商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private Category requireCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }

    private CategoryVO toVO(Category c) {
        return new CategoryVO(c.getId(), c.getParentId(), c.getName(), c.getSortOrder(), c.getStatus());
    }
}
