# 04 JWT 鉴权与 RBAC 权限方案

> 版本：v1.0 ｜ 作者：成员 A（后端核心）｜ 日期：2026-08-12
> 依据：启动指导书第 2、6 节 + `.qoder/rules/api-contract.md`（团队共享） + `.qoder/members/member-a/rules/jwt-rbac.md`（A 专属）
> 本方案是第二阶段后端鉴权实现（拦截器/UserContext）与成员 C 权限矩阵的直接依据。

## 0. 方案总览

- **模型**：JWT 无状态认证 + RBAC 三角色授权（USER / MERCHANT / ADMIN），单体后端统一签发校验。
- **原则**：**前端隐藏按钮只是体验优化，真正的权限判断必须在后端完成**（api-contract.md Authorization 条款）。
- **校验顺序**（jwt-rbac.md）：先鉴权（token/角色）→ 再校验资源归属 → 再校验状态流转（T1）→ 执行业务。

## 1. 角色定义与判定规则

| 角色 | 枚举值 | 判定规则 | 数据范围 | 接口前缀 |
|---|---|---|---|---|
| 普通用户 | USER | users.role = USER | 仅自己的订单/售后/地址/购物车/通知 | /api/** |
| 商家 | MERCHANT | users.role = MERCHANT | 仅自己店铺的商品/订单/售后/统计 | /api/merchant/** |
| 管理员 | ADMIN | users.role = ADMIN | 全局（审核/治理/介入/统计） | /api/admin/** |

**角色判定要点**：

1. 角色在注册时确定（本版不做自助升级），由种子数据/管理员操作维护。
2. 商家角色必须绑定店铺：JWT 签发时从 merchant_shops 取 shopId 写入 claims；无店铺的 MERCHANT 账号拒绝签发 token（返回 6001 店铺不存在）。
3. 管理员账号由系统初始化（种子数据），不开放注册通道。

## 2. JWT 规则

### 2.1 传输与有效期

| 项 | 约定 |
|---|---|
| 请求头 | `Authorization: Bearer <token>` |
| 有效期 | 24 小时（课程项目约定，api-contract.md） |
| 过期处理 | 后端返回 1002 未登录；前端清 token 并跳转登录页 |
| 密钥 | 后端配置文件（application.yml 环境变量注入），禁止硬编码进仓库 |
| 算法 | HS256（课程项目从简；如用非对称请声明 RS256） |

### 2.2 claims 结构

```json
{
  "sub": "10001",
  "role": "MERCHANT",
  "shopId": "20001",
  "iat": 1754323200,
  "exp": 1754409600
}
```

| claim | 含义 | 说明 |
|---|---|---|
| sub | 用户ID | users.id |
| role | 角色 | USER / MERCHANT / ADMIN |
| shopId | 店铺ID | 仅 MERCHANT 签发时携带；USER/ADMIN 不携带（null） |
| iat | 签发时间 | 秒级时间戳 |
| exp | 过期时间 | iat + 24h |

### 2.3 签发与校验流程

1. **签发**：登录成功（POST /api/users/login）→ 后端校验用户名密码 → 查角色 → 商家角色查店铺 → 组装 claims → 签名返回 `{token, userInfo}`。
2. **校验**：`AuthInterceptor` 解析 Bearer token → 验签 → 校验 exp → 注入 `UserContext`（userId/role/shopId）。
3. **前端约定**：只保存 token，不做业务判断；token 过期收到 1002 后清除本地 token 跳登录。

## 3. 接口前缀与拦截矩阵

### 3.1 白名单（匿名可访问）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/users/register | 注册 |
| POST | /api/users/login | 登录 |
| GET | /api/products | 商品列表（公开浏览） |
| GET | /api/products/{id} | 商品详情（公开浏览） |
| GET | /api/categories | 类目树（公开浏览） |

> 白名单仅限上述 GET 公开资源与认证接口；其余一律要求登录。

### 3.2 拦截矩阵（后端强制）

| 前缀 | 放行角色 | 校验内容 | 失败返回 |
|---|---|---|---|
| 白名单 | 匿名 | 无 | - |
| /api/**（用户资源） | 三角色均可 | token 有效；资源归属当前用户（见第 4 节清单） | 1002 / 1003 |
| /api/merchant/** | 仅 MERCHANT | 角色 + shopId 存在 + 资源归属当前店铺 | 1002 / 1003 / 6004 |
| /api/admin/** | 仅 ADMIN | 角色校验 | 1002 / 7004 |

**拦截器实现约定**：

- 三个拦截器或一个带注解的方案均可，本版约定：`AuthInterceptor`（全局）+ `@RequireMerchant` / `@RequireAdmin` 注解（Controller 层标注）。
- 拦截失败统一返回统一信封 `{code, message, data, total}`，HTTP 200。
- 错误码：token 无效/过期 → 1002 未登录；角色不符 → 1003 无权限；商家资源越权 → 6004 非本店数据；非管理员访问 admin → 7004 无管理员权限。

## 4. 数据权限校验清单（后端强制，前端仅隐藏按钮）

| # | 操作 | 校验条件 |
|---|---|---|
| 1 | 订单列表/详情/取消/确认收货 | `order.user_id == UserContext.userId` |
| 2 | 购物车增删改查 | `cart.user_id == UserContext.userId` |
| 3 | 地址增删改查/设默认 | `address.user_id == UserContext.userId` |
| 4 | 退款申请/撤销/列表 | `refund.user_id == UserContext.userId` |
| 5 | 通知列表/标记已读 | `notice.receiver_id == UserContext.userId` |
| 6 | 商家订单列表/发货 | `order.merchant_id == UserContext.shopId` |
| 7 | 商家退款列表/同意/拒绝 | `refund.merchant_id == UserContext.shopId` |
| 8 | 商品创建/编辑/上下架/库存 | `product.merchant_id == UserContext.shopId` |
| 9 | 店铺资料编辑 | `shop.user_id == UserContext.userId` 且 audit_status 合法 |
| 10 | 管理员审核/裁决/强制下架/统计 | 全局可见；敏感操作必须写 audit_logs（第 5 节清单） |

**校验失败返回**：用户资源越权 → 1003 无权限（或资源专用码如 2005 地址不存在）；商家资源越权 → 6004 非本店数据。

## 5. 敏感操作审计清单（写入 audit_logs）

| 操作 | operator_role | target_type | action |
|---|---|---|---|
| 商家入驻审核通过/驳回 | ADMIN | MERCHANT | APPROVE / REJECT |
| 商品上架审核通过/驳回 | ADMIN | PRODUCT | APPROVE / REJECT |
| 管理员强制下架商品 | ADMIN | PRODUCT | TAKE_DOWN |
| 平台介入退款裁决 | ADMIN | REFUND | HANDLE_REFUND |
| 商家发货 | MERCHANT | ORDER | SHIP |
| 商家同意/拒绝退款 | MERCHANT | REFUND | AGREE / REJECT |

**约定**：管理员与商家的上述状态变更必须写 audit_logs（operator_id/operator_role/target_type/target_id/action/remark）；用户侧操作（取消订单/确认收货）本版不强制写，后续可扩展。

## 6. 与 T1 状态机联动

1. 鉴权通过后，状态流转合法性按 T1 状态机在 Service 层校验（显式业务方法 payOrder/shipOrder/agreeRefund 等）。
2. 非法流转返回 4002/5005/3005 等状态类错误码（见 T3 第 8 节对照表），与 1001 参数错误区分。
3. 校验顺序固定：鉴权 → 资源归属 → 状态流转 → 业务执行；任何一步失败立即拒绝，不执行后续逻辑。

## 7. 与成员 C 的权限矩阵对齐约定

1. C 按本方案第 1 节角色表与第 4 节数据权限清单编写后台权限矩阵（页面 → 按钮 → 接口 → 角色）。
2. 前端按钮隐藏规则由 C 定义；后端拦截规则以本方案为准。
3. 双方如有分歧，在 T5 接口清单评审时统一裁定，由 A 更新本方案并升版。

## 8. 差异记录与决策

对照 `.qoder/rules/api-contract.md` 核对：Bearer 头、24 小时有效期、后端校验、前缀约定全部一致，无差异。
补充约定：商家无店铺时拒绝签发 token（返回 6001）；管理员账号不开放注册。

## 9. 验收自查

- [x] 三角色判定与数据范围边界清晰（用户看自己、商家看本店、管理员看全局）
- [x] token 结构（claims）、有效期 24h、过期处理（1002 重新登录）明确
- [x] 拦截矩阵覆盖 /api/、/api/merchant/、/api/admin/ 三个前缀 + 白名单
- [x] 明确"前端隐藏按钮 + 后端强制校验"双保险原则（第 0 节 + 第 4 节）
- [x] 敏感操作（退款处理、商家审核等）约定写入 audit_logs（第 5 节清单）
