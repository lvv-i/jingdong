package com.example.shop.controller.admin;

import com.example.shop.common.ApiResult;
import com.example.shop.dto.CategoryCreateDTO;
import com.example.shop.dto.CategoryUpdateDTO;
import com.example.shop.service.CategoryService;
import com.example.shop.vo.CategoryVO;
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
 * 管理员类目控制器（A-004~A-007；仅 ADMIN）
 * A-007 软删除：有商品时拒绝并提示
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /** A-004 全量列表（含禁用类目） */
    @GetMapping
    public ApiResult<List<CategoryVO>> list() {
        return ApiResult.success(categoryService.listAll());
    }

    /** A-005 新建类目 */
    @PostMapping
    public ApiResult<Map<String, Long>> create(@Valid @RequestBody CategoryCreateDTO dto) {
        return ApiResult.success(Map.of("id", categoryService.create(dto)));
    }

    /** A-006 编辑类目（name/sortOrder/status） */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateDTO dto) {
        categoryService.update(id, dto);
        return ApiResult.success();
    }

    /** A-007 软删除类目（有商品时拒绝并提示） */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResult.success();
    }
}
