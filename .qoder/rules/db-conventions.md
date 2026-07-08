# Rule: Database Conventions

## Scope

Applies to all MySQL tables, fields, indexes, and migration scripts for the e-commerce platform.

## Table Naming

- Use lowercase plural nouns in snake_case.
- Examples: `users`, `products`, `order_items`, `refund_requests`, `merchant_shops`.
- Association tables combine both entity names: `user_roles`, `product_categories` if needed.

## Mandatory Common Fields

Every business table must include these fields at the end of the column list:

```sql
`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
`status` VARCHAR(32) NOT NULL COMMENT '业务状态',
`deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除'
```

Pure link tables without business state may omit `status`, but must keep the other four fields.

## Field Naming

- Lowercase snake_case.
- Foreign keys use `<entity>_id`, e.g., `user_id`, `merchant_id`, `order_id`.
- Boolean flags use descriptive names, e.g., `is_default`, `deleted_flag`.
- Avoid abbreviations except widely known ones: `qty` is not allowed; use `quantity`.

## Data Types

| Purpose | Type |
|---------|------|
| Primary key | BIGINT UNSIGNED AUTO_INCREMENT |
| Foreign key | BIGINT UNSIGNED NOT NULL |
| Money | DECIMAL(12,2) NOT NULL |
| Quantity / stock | INT UNSIGNED NOT NULL DEFAULT 0 |
| Status | VARCHAR(32) NOT NULL |
| Long text | TEXT |
| Boolean / flag | TINYINT NOT NULL DEFAULT 0 |
| JSON data | JSON or TEXT with validation |
| Time | DATETIME |

## Soft Deletion

- Use `deleted_flag` = 1 for deletion; do not use physical `DELETE` on business tables.
- Every query must include `WHERE deleted_flag = 0` unless the feature explicitly needs deleted data.
- Admin recovery feature is the only exception for querying `deleted_flag = 1`.

## Snapshot Fields

Historical values must be frozen at the time of the transaction:

- `order_item` must store `title_snapshot` and `price_snapshot`.
- `refund_request` must store `reason`, `merchant_reply`, and `admin_result`.
- Never allow product price or title changes to affect existing orders.

## Indexes

Add indexes for:

- Primary keys (automatic).
- Foreign keys.
- Status fields used in WHERE clauses.
- Time fields used for sorting or filtering.
- Search fields such as `title` when full-text or prefix search is required.

Example:

```sql
CREATE INDEX idx_user_status ON orders(user_id, status);
CREATE INDEX idx_created_at ON orders(created_at);
```

## Comments

- Every table must have a Chinese comment.
- Every column must have a Chinese comment.
- Status columns must document all possible values in the comment.

Example:

```sql
CREATE TABLE orders (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `status` VARCHAR(32) NOT NULL COMMENT '状态：PENDING_PAY待支付 PAID已支付待发货 SHIPPED已发货待收货 COMPLETED已完成 CANCELLED已取消',
  ...
) COMMENT='订单主表';
```

## Migration Scripts

- Place each table creation in its own file or in a logically grouped migration.
- Name migrations with date prefix: `20260708_001_create_orders.sql`.
- Include rollback statements commented out or in a separate `down` file.
- Seed data must be in separate `seed` scripts.

## Forbidden Practices

- No physical delete on business tables.
- No nullable foreign keys unless business explicitly allows orphan records.
- No duplicate index columns.
- No use of `TEXT` for short strings.
- No table without `created_at` and `updated_at`.
