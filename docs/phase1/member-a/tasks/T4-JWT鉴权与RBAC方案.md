# T4 JWT 鉴权与 RBAC 权限方案

> 成员 A ｜ 计划 Day 2 ｜ 状态：⬜ 未开始
> 关联 rule：`.qoder/members/member-a/rules/jwt-rbac.md`（A 专属）、`.qoder/rules/api-contract.md`（团队共享）

## 目标

产出 JWT 鉴权与 RBAC 权限方案定稿：三个角色、token 规则、接口前缀与拦截规则、数据权限边界。该方案是第二阶段后端鉴权实现与 C 权限矩阵的直接依据。

## 输入依赖

- 启动指导书第 2、6 节（鉴权 JWT + 角色权限控制；RBAC；权限必须在后端校验）
- `.qoder/rules/api-contract.md`（Bearer 头、24 小时过期、前缀约定）
- T1 状态机枚举表（状态操作对应的角色）

## 工作步骤

1. 定义三个角色与判定规则：
   - `USER`（普通用户）：只能访问自己的订单/售后/地址/购物车
   - `MERCHANT`（商家）：只能访问自己店铺的商品/订单/售后
   - `ADMIN`（管理员）：可跨店铺、跨用户访问（审核、治理、介入、统计）
2. 定义 JWT 规则：
   - 请求头：`Authorization: Bearer <token>`；有效期 24 小时
   - claims 结构：sub（用户ID）、role、shopId（商家角色时携带店铺ID）、iat/exp
   - 密钥与签发位置：后端统一签发；前端只保存不解析业务逻辑
3. 定义接口前缀与拦截规则：
   - 白名单：登录、注册、公开商品浏览（GET /api/products 等）
   - `/api/**`：登录用户（USER/MERCHANT/ADMIN 均可）
   - `/api/merchant/**`：仅 MERCHANT，且校验资源归属当前店铺
   - `/api/admin/**`：仅 ADMIN
4. 定义数据权限校验清单（后端强制，前端只隐藏按钮）：
   - 订单详情/取消/确认收货：订单归属当前用户
   - 商家发货/退款处理：订单/退款归属当前店铺
   - 管理员介入：任意数据但操作必须写 audit_logs
5. 与 C 的权限矩阵对齐：C 按本方案编写权限矩阵，A 按本方案实现拦截器。

## 产出物

- 文件：`docs/phase1/member-a/deliverables/04-JWT鉴权与RBAC方案.md`
- 内容：角色表、JWT 结构与生命周期、前缀拦截矩阵、数据权限校验清单

## 验收标准

- [ ] 三角色判定与数据范围边界清晰（用户看自己、商家看本店、管理员看全局）
- [ ] token 结构（claims）、有效期、过期后的处理（1002 重新登录）明确
- [ ] 拦截矩阵覆盖 /api/、/api/merchant/、/api/admin/ 三个前缀
- [ ] 明确"前端隐藏按钮 + 后端强制校验"双保险原则
- [ ] 敏感操作（退款处理、商家审核）约定写入 audit_logs

## 状态

- [ ] 未开始　[ ] 进行中　[ ] 已完成
