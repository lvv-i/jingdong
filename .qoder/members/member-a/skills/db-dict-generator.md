# Skill (A专属): 数据字典生成与审查

## Description

指导成员 A 生成/审查数据字典 v1.0：核心 11 张表的完整字段清单。输出是第二阶段建表 SQL 的唯一依据，也是 B/C/D 联调的字段口径。

## When to Use

- 第一阶段 T2：产出数据字典 v1.0
- 审查他人提交的建表 SQL 或字段变更请求
- 第二阶段生成 CREATE TABLE 脚本

## 覆盖表清单（11 张）

users、merchant_shops、categories、products、cart_items、orders、order_items、refund_requests、addresses、notices、audit_logs

## 输出模板

每张表：

### 表头信息

| 项目 | 内容 |
|------|------|
| 表名 | orders |
| 中文名 | 订单主表 |
| 用途 | 一笔订单一行，含金额与状态 |

### 字段清单

| 字段 | 类型 | 必填 | 默认值 | 中文注释 | 索引 |
|------|------|------|--------|----------|------|
| id | BIGINT UNSIGNED | 是 | 自增 | 主键 | PK |
| order_no | VARCHAR(32) | 是 | - | 订单编号 | UNIQUE |
| user_id | BIGINT UNSIGNED | 是 | - | 用户ID | INDEX |
| pay_amount | DECIMAL(12,2) | 是 | 0.00 | 实付金额 | - |
| status | VARCHAR(32) | 是 | PENDING_PAY | 状态：PENDING_PAY待支付 PAID已支付待发货 SHIPPED已发货待收货 COMPLETED已完成 CANCELLED已取消 | INDEX |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 | INDEX |
| ... | ... | ... | ... | ... | ... |

## 强制核查项（与 .qoder/rules/db-conventions.md 对齐）

1. **统一字段**：每张业务表含 id/created_at/updated_at/status/deleted_flag
2. **快照**：order_items 必含 title_snapshot、price_snapshot；refund_requests 必含 reason、merchant_reply、admin_result
3. **类型**：金额 DECIMAL(12,2)；数量/库存 INT UNSIGNED；status VARCHAR(32)；布尔 TINYINT(0/1)
4. **命名**：下划线小写；外键 `<entity>_id`；禁用缩写（qty→quantity）
5. **软删除**：业务表只软删，所有查询 WHERE deleted_flag = 0
6. **索引**：外键、status、created_at、order_no 必须建索引
7. **注释**：每表每列中文注释；status 注释列出全部取值（引用 T1 状态机）
8. **审核留痕**：merchant_shops 必含 audit_status、audit_reason；audit_logs 记录操作者角色

## 审查输出格式

对每张表输出：字段清单核查表（字段/类型/注释/索引/结论）→ 问题清单与修正 SQL 片段 → 索引建议。
