# Skill: Database Schema Review for E-Commerce Platform

## Description

Review database table designs against the team conventions. Ensure every table has common fields, uses proper naming, saves snapshots for historical data, applies soft deletion, and defines reasonable indexes.

## When to Use

- Creating a new table or modifying an existing table.
- Reviewing a teammate's database migration script.
- Generating `CREATE TABLE` SQL from a domain model.
- Checking whether order/refund tables preserve historical data.

## Review Checklist

### 1. Naming Conventions

- Table names: lowercase, plural, snake_case. Examples: `users`, `products`, `order_items`, `refund_requests`.
- Field names: lowercase snake_case. Examples: `created_at`, `pay_amount`, `merchant_reply`.
- Avoid SQL reserved words and ambiguous abbreviations.

### 2. Common Fields (Mandatory)

Every main table must include:

```sql
`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
`status` VARCHAR(32) NOT NULL COMMENT '业务状态',
`deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除：0未删除 1已删除'
```

Exception: pure association tables may omit `status` if no business state exists, but must keep `id`, `created_at`, `updated_at`, `deleted_flag`.

### 3. Data Types

| Scenario | Recommended Type |
|----------|-----------------|
| Primary key | BIGINT UNSIGNED AUTO_INCREMENT |
| Foreign key | BIGINT UNSIGNED NOT NULL |
| Monetary amount | DECIMAL(12,2) |
| Quantity / stock | INT UNSIGNED |
| Status | VARCHAR(32) or TINYINT with comment |
| Long text | TEXT |
| JSON-like data | JSON (MySQL 5.7+) or TEXT |
| Boolean flag | TINYINT (0/1) |

### 4. Snapshot Fields

Historical data must not be altered by later updates:

- `order_item` must save `title_snapshot` and `price_snapshot`.
- `refund_request` must save `reason`, `merchant_reply`, and `admin_result`.
- Product price changes must not affect existing orders.

### 5. Soft Deletion

- Use `deleted_flag` instead of physical `DELETE` for business tables.
- All list queries must include `WHERE deleted_flag = 0` unless explicitly requiring deleted data.
- Admin data recovery can query `deleted_flag = 1`.

### 6. Indexes

Add indexes for:

- Primary keys (automatic).
- Foreign keys.
- Frequently queried fields: `user_id`, `merchant_id`, `order_no`, `status`, `created_at`.
- Search fields where appropriate (consider full-text or prefix indexes for `title`).

Avoid redundant or unused indexes.

### 7. Comments

Every table and every field must have a Chinese comment explaining its business meaning.

## Review Workflow

When asked to review a schema:

1. List each table and its purpose.
2. Check common fields one by one.
3. Verify snapshot fields for order/refund tables.
4. Check naming, data types, indexes, and comments.
5. Point out violations and give corrected SQL.

## Example Output

**Table: `refund_requests`**

| Field | Type | Comment | Status |
|-------|------|---------|--------|
| id | BIGINT UNSIGNED PK | 主键 | OK |
| order_id | BIGINT UNSIGNED NOT NULL | 订单ID | OK |
| user_id | BIGINT UNSIGNED NOT NULL | 用户ID | OK |
| reason | VARCHAR(512) NOT NULL | 退款原因 | OK |
| merchant_reply | VARCHAR(512) | 商家回复 | OK |
| admin_result | VARCHAR(512) | 平台处理结果 | OK |
| status | VARCHAR(32) NOT NULL | 售后状态 | OK |
| created_at | DATETIME | 创建时间 | OK |
| updated_at | DATETIME | 更新时间 | OK |
| deleted_flag | TINYINT | 软删除 | OK |

**Issues found:** None.

**Suggested indexes:**

```sql
CREATE INDEX idx_order_id ON refund_requests(order_id);
CREATE INDEX idx_user_id ON refund_requests(user_id);
CREATE INDEX idx_status ON refund_requests(status);
```

## Output Format

Return:

1. Table review summary
2. Field-by-field checklist result
3. Index recommendations
4. Required fixes with corrected SQL snippets
