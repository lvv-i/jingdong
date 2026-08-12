# Rule (A专属): JWT 鉴权与 RBAC 实现约束

## Scope

适用于成员 A 的 JWT 签发、拦截器、角色校验与数据权限校验实现。第一阶段 T4 方案定稿后，本 rule 是第二阶段实现的行为约束。

## 三个角色（RBAC）

| 角色 | 枚举值 | 数据范围 | 接口前缀 |
|------|--------|----------|----------|
| 普通用户 | USER | 仅自己的订单/售后/地址/购物车/通知 | /api/** |
| 商家 | MERCHANT | 仅自己店铺的商品/订单/售后/统计 | /api/merchant/** |
| 管理员 | ADMIN | 全局（审核/治理/介入/统计） | /api/admin/** |

## JWT 规则

- 请求头：`Authorization: Bearer <token>`
- 有效期：24 小时（课程项目约定）；过期返回 1002 未登录
- claims 约定：sub=用户ID、role=USER|MERCHANT|ADMIN、shopId=店铺ID（仅商家签发时携带）、iat、exp
- 密钥：后端配置文件管理，禁止硬编码在代码仓库提交（.env 已在 .gitignore）
- 前端只保存 token，不做业务判断；按钮隐藏只是体验优化

## 拦截器矩阵（后端强制）

| 路径 | 放行角色 | 校验内容 |
|------|----------|----------|
| 白名单（登录/注册/公开商品浏览/类目） | 匿名 | 仅限 GET 公开资源与认证接口 |
| /api/** | 已登录（三角色均可） | token 有效；用户资源校验归属当前用户 |
| /api/merchant/** | 仅 MERCHANT | 角色 + shopId 存在 + 资源归属当前店铺 |
| /api/admin/** | 仅 ADMIN | 角色校验 |

## 数据权限强制清单（不能只靠前端隐藏）

1. 订单详情/取消/确认收货：`order.user_id == 当前用户`
2. 退款申请/撤销：`refund.user_id == 当前用户`
3. 商家发货/退款处理：`order.merchant_id == 当前店铺`（且订单状态合法）
4. 商品编辑/上下架/库存：`product.merchant_id == 当前店铺`
5. 管理员操作：全局可见，但所有状态变更与审核操作必须写 audit_logs（operator_id、operator_role、target_type、target_id、action、remark）

## 与状态机联动

- 鉴权通过后，状态流转合法性按 T1 状态机校验（Service 层），非法流转返回 1001 并拒绝
- 状态变更与权限判定顺序：先鉴权 → 再校验资源归属 → 再校验状态流转
