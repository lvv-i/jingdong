# Skill (A专属): 接口清单生成与审查

## Description

指导成员 A 生成/审查接口清单 v1.0：汇总 B/C/D 的接口需求，产出四组（公共/用户/商家/管理员）全量 API 列表，作为后端实现与前端调用的共同合同。

## When to Use

- 第一阶段 T5：汇总需求产出接口清单 v0.9 → v1.0
- B/C/D 提交接口需求后对齐评审
- 第二阶段编写 Controller 前查阅

## 输入处理流程

1. 收集 B/C/D 接口需求（页面清单 → 接口映射）
2. 按角色分组归类：公共 / 用户(/api/前缀) / 商家(/api/merchant/前缀) / 管理员(/api/admin/前缀)
3. 合并重复项、补齐缺失项（对照启动指导书第 6 节示例接口）
4. 逐接口填模板

## 接口输出模板

| 编号 | 模块 | 方法与URL | 访问角色 | 请求参数/体 | 成功响应(示例) | 错误码 | 备注 |
|------|------|-----------|----------|-------------|----------------|--------|------|
| A-001 | 用户 | POST /api/orders | USER | addressId, cartItemIds, remark | {code:200,message:"下单成功",data:{orderId,orderNo,status:PENDING_PAY,payAmount}} | 1001/2004/3005/4001 | 状态 PENDING_PAY；校验库存与价格快照 |

要点：
- 响应一律统一返回格式 `{code, message, data, total}`；分页接口 data 为 `{list, total}` 且参数固定 page/pageSize
- 错误码来自 T3 错误码分段表
- 涉及状态流转的接口在备注标注 `from → to`（对应 T1）
- 权限与数据范围标注（对应 T4）：如"仅本店铺数据"

## 分组与最小覆盖清单

- **公共**：POST /api/auth/login、POST /api/auth/register、GET /api/categories、GET /api/products、GET /api/products/{id}
- **用户端**：购物车增删改查、POST /api/orders、GET /api/orders、GET /api/orders/{id}、POST /api/orders/{id}/cancel、POST /api/orders/{id}/pay(模拟)、POST /api/orders/{id}/confirm、POST /api/refunds、GET /api/refunds、评价、地址 CRUD、GET /api/notifications、个人中心
- **商家端**：商品发布/上下架/库存、GET /api/merchant/orders、POST /api/merchant/orders/{id}/ship、POST /api/merchant/refunds/{id}/agree|reject、店铺资料、统计
- **管理员端**：POST /api/admin/merchants/{id}/approve|reject、类目 CRUD、商品巡检下架、GET /api/admin/orders、POST /api/admin/refunds/{id}/handle、用户管理、日志统计

## 版本管理

- v0.9：初稿，供 B/C/D 试用核对
- v1.0：定稿，头部标注版本号、日期、变更记录；此后任何变更须升版本并通知全员

## 审查清单

1. 统一返回格式与分页契约（.qoder/rules/api-contract.md）
2. 每个接口有访问角色与数据范围
3. 错误码在 T3 分段内且无重复
4. 状态流转与 T1 一致
5. 敏感操作（退款、审核、支付）有日志与幂等说明
