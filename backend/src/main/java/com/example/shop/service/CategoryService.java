package com.example.shop.service;

import com.example.shop.dto.CategoryCreateDTO;
import com.example.shop.dto.CategoryUpdateDTO;
import com.example.shop.vo.CategoryVO;

import java.util.List;

/**
 * 类目服务
 * 接口映射：P-003 公开类目树 / A-004 全量列表 / A-005 新建 / A-006 编辑 / A-007 删除
 */
public interface CategoryService {

    /** P-003 公开类目树（仅 ENABLED） */
    List<CategoryVO> listEnabled();

    /** A-004 管理员全量列表（含 DISABLED） */
    List<CategoryVO> listAll();

    /** A-005 新建类目 */
    Long create(CategoryCreateDTO dto);

    /** A-006 编辑类目（name/sortOrder/status） */
    void update(Long id, CategoryUpdateDTO dto);

    /** A-007 软删除类目（有商品时拒绝） */
    void delete(Long id);
}
