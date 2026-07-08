# Skill: Spring Boot CRUD Generator

## Description

Generate consistent Spring Boot CRUD scaffolding from a given entity class. The output follows the team layering convention: Controller -> Service -> ServiceImpl -> Mapper -> XML, with unified response envelope and page helper pagination.

## When to Use

- Starting a new business module on the backend.
- Creating boilerplate code for `users`, `products`, `orders`, `refund_requests`, etc.
- Standardizing an existing controller/service to the team contract.

## Layering Convention

```
com.example.shop
  controller
    ProductController.java
  service
    ProductService.java
  service.impl
    ProductServiceImpl.java
  mapper
    ProductMapper.java
  entity
    Product.java
  dto
    ProductDTO.java
    ProductQuery.java
  vo
    ProductVO.java
```

## Technology Stack

- Spring Boot 3.x
- MyBatis / MyBatis-Plus
- PageHelper for pagination
- Lombok for reducing boilerplate
- Jakarta validation annotations

## Entity Requirements

The entity must contain at minimum:

```java
@Data
public class Product {
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String title;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String mainImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deletedFlag;
}
```

## CRUD Operations to Generate

For each entity, generate:

1. **Create** - `POST /api/<module>`
2. **Update** - `PUT /api/<module>/{id}`
3. **Delete** - `DELETE /api/<module>/{id}` (soft delete: set `deleted_flag = 1`)
4. **Get by id** - `GET /api/<module>/{id}`
5. **List page** - `GET /api/<module>?page=1&pageSize=10`

## Code Templates

### Controller

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResult<Long> create(@Valid @RequestBody ProductDTO dto) {
        Long id = productService.create(dto);
        return ApiResult.success(id);
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        productService.update(id, dto);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResult.success();
    }

    @GetMapping("/{id}")
    public ApiResult<ProductVO> getById(@PathVariable Long id) {
        return ApiResult.success(productService.getById(id));
    }

    @GetMapping
    public ApiResult<PageResult<ProductVO>> list(ProductQuery query) {
        return ApiResult.success(productService.list(query));
    }
}
```

### Service Interface

```java
public interface ProductService {
    Long create(ProductDTO dto);
    void update(Long id, ProductDTO dto);
    void delete(Long id);
    ProductVO getById(Long id);
    PageResult<ProductVO> list(ProductQuery query);
}
```

### ServiceImpl

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Long create(ProductDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("DRAFT");
        entity.setDeletedFlag(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, ProductDTO dto) {
        Product entity = productMapper.selectById(id);
        if (entity == null || entity.getDeletedFlag() == 1) {
            throw new BusinessException(1004, "商品不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        productMapper.softDelete(id, LocalDateTime.now());
    }

    @Override
    public ProductVO getById(Long id) {
        Product entity = productMapper.selectById(id);
        if (entity == null || entity.getDeletedFlag() == 1) {
            throw new BusinessException(1004, "商品不存在");
        }
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public PageResult<ProductVO> list(ProductQuery query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        List<Product> list = productMapper.selectList(query);
        PageInfo<Product> pageInfo = new PageInfo<>(list);
        List<ProductVO> voList = pageInfo.getList().stream()
            .map(e -> {
                ProductVO vo = new ProductVO();
                BeanUtils.copyProperties(e, vo);
                return vo;
            }).collect(Collectors.toList());
        return new PageResult<>(voList, pageInfo.getTotal());
    }
}
```

### Mapper

```java
@Mapper
public interface ProductMapper {
    int insert(Product product);
    int updateById(Product product);
    Product selectById(Long id);
    List<Product> selectList(@Param("query") ProductQuery query);
    int softDelete(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
}
```

### XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.shop.mapper.ProductMapper">

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO product (merchant_id, category_id, title, price, stock, status, main_image, created_at, updated_at, deleted_flag)
        VALUES (#{merchantId}, #{categoryId}, #{title}, #{price}, #{stock}, #{status}, #{mainImage}, #{createdAt}, #{updatedAt}, #{deletedFlag})
    </insert>

    <update id="updateById">
        UPDATE product
        SET title = #{title}, price = #{price}, stock = #{stock}, status = #{status}, main_image = #{mainImage}, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted_flag = 0
    </update>

    <update id="softDelete">
        UPDATE product SET deleted_flag = 1, updated_at = #{updatedAt} WHERE id = #{id}
    </update>

    <select id="selectById" resultType="com.example.shop.entity.Product">
        SELECT * FROM product WHERE id = #{id} AND deleted_flag = 0
    </select>

    <select id="selectList" resultType="com.example.shop.entity.Product">
        SELECT * FROM product
        WHERE deleted_flag = 0
        <if test="query.merchantId != null">
            AND merchant_id = #{query.merchantId}
        </if>
        <if test="query.status != null and query.status != ''">
            AND status = #{query.status}
        </if>
        <if test="query.keyword != null and query.keyword != ''">
            AND title LIKE CONCAT('%', #{query.keyword}, '%')
        </if>
        ORDER BY created_at DESC
    </select>
</mapper>
```

## Workflow

When generating CRUD for an entity:

1. Confirm the entity fields and module name.
2. Ask whether the module is user-facing, merchant-facing, or admin-facing to set the correct URL prefix.
3. Generate Controller, Service, ServiceImpl, Mapper, and XML.
4. Highlight which fields are required and which have default values.
5. Remind the user to add indexes and validation rules.

## Output Format

Return Java and XML code blocks for each layer. Add a short note about URL prefix, role access, and any entity-specific business logic that needs manual adjustment.
