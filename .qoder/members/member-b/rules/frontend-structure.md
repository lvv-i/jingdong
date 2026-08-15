# Rule (B专属): 用户端工程结构规范

## Scope

适用于成员 B 编写的 `frontend-user/` 用户网页端工程（Vue 3 + Vite + Element Plus + Vue Router + Pinia + Axios）。在团队 `.qoder/rules/code-style.md` 与 `.qoder/skills/frontend-component-guide.md` 之上的用户端细化规范。

## 工程目录结构

```
frontend-user/
├── index.html
├── vite.config.js            # server.proxy → 后端；@ 别名 → src
├── package.json
└── src/
    ├── main.js               # 挂载 ElementPlus、Router、Pinia
    ├── App.vue               # 全局布局：顶部导航 + <router-view>
    ├── api/                  # 按模块封装接口（与 T5 分组对应）
    │   ├── request.js        # Axios 实例 + 拦截器（api-integration skill）
    │   ├── auth.js           # P-001/P-002/P-007/P-008
    │   ├── product.js        # P-003~P-006
    │   ├── cart.js           # U-008~U-011
    │   ├── order.js          # U-012~U-017、U-024
    │   ├── refund.js         # U-018~U-021
    │   ├── user.js           # U-001/U-002
    │   ├── address.js        # U-003~U-007
    │   └── notification.js   # U-022/U-023/U-025
    ├── router/
    │   └── index.js          # 13 页路由表 + beforeEach 守卫
    ├── stores/
    │   └── user.js           # token/userInfo 持久化 + logout
    ├── utils/
    │   ├── status-map.js     # 订单/售后状态 → 中文/标签色（只读 T1）
    │   └── format.js         # 金额/日期格式化
    ├── components/           # 公共组件
    │   ├── ProductCard.vue   # 商品卡片（首页/搜索共用）
    │   ├── StatusTag.vue     # 订单/售后状态标签
    │   └── EmptyState.vue    # 空状态组件（P1 验收）
    ├── views/                # 13 页视图（目录与路由一致）
    │   ├── Home.vue
    │   ├── Search.vue
    │   ├── ProductDetail.vue
    │   ├── Login.vue
    │   ├── Cart.vue
    │   ├── Checkout.vue
    │   ├── OrderList.vue
    │   ├── OrderDetail.vue
    │   ├── RefundCenter.vue
    │   ├── Review.vue
    │   ├── Profile.vue
    │   ├── AddressList.vue
    │   └── Notifications.vue
    └── assets/
```

## 分层职责（不可越层）

| 层 | 职责 | 禁止 |
|----|------|------|
| views | 页面组装：调 api、绑数据、处理交互 | 直接写 fetch/axios；重复的业务判断下沉到 utils |
| components | 纯展示组件，props 传入 | 自己发请求（除确认弹窗类） |
| api | 接口调用与错误码分支（request.js 全局 + 各模块文件） | 在 view 里散落 URL 字符串 |
| stores | 全局状态：token、userInfo、购物车角标 | 存接口无关的页面局部状态 |
| utils | 状态映射、格式化等纯函数 | 操作 DOM、发请求 |

## 关键约定

1. **路由**：13 页与 B-01 清单一一对应（`/`、`/search`、`/product/:id`、`/login`、`/cart`、`/checkout`、`/orders`、`/orders/:id`、`/refunds`、`/orders/:id/review`、`/profile`、`/addresses`、`/notifications`），不新增业务路由。
2. **鉴权**：`beforeEach` 守卫未登录跳 `/login`；已登录访问 `/login` 跳首页；token 存 localStorage（Pinia 持久化）。
3. **状态展示**：`utils/status-map.js` 是订单/售后状态文案的唯一来源，值与 T1 完全一致；按钮显隐由后端返回 status 驱动，不写死。
4. **错误处理**：全局拦截器兜底 toast + 1002 跳登录；页面只处理需要改变交互的错误分支（刷新/回跳/局部提示）。
5. **分页**：列表页一律 `el-pagination` + `page/pageSize`（pageSize ≤ 100），取 `data.list`/`data.total`。
6. **空状态**：所有列表/详情无数据必须渲染 `EmptyState` 或 `el-empty` 且有文案（P1 验收项）。
7. **命名**：组件 PascalCase（`ProductCard.vue`）、目录/路由 kebab-case、变量 camelCase；api 文件方法名与 T5 接口编号注释对应。
8. **依赖**：UI 只用 Element Plus，不自引其他 UI 库；图标用 Element Plus Icons。

## 越界检查（我是用户端）

- 不实现商家/管理员界面（那是 C 的职责）。
- 不新增后端接口；发现契约缺口先向 A 提需求决议，不自造接口。
- 不修改 `.qoder/` 团队共享配置（维护人是 D）。
# Rule (B专属): 用户端工程结构与分层规范

> 仅约束成员 B 产出的用户网页端代码（`frontend-user/`）。团队共享 `.qoder/rules/code-style.md` 同样强制。

## 目录结构

```
frontend-user/
├── index.html
├── vite.config.js          # dev proxy: /api → http://localhost:8080
├── package.json
└── src/
    ├── main.js             # 挂载 Element Plus / router / pinia
    ├── App.vue
    ├── api/                # 按 T5 分组：public.js / user.js（方法名带接口编号注释）
    ├── router/index.js     # 13 页路由表 + beforeEach 守卫
    ├── stores/user.js      # token/userInfo/localStorage 持久化
    ├── utils/request.js    # Axios 实例 + 拦截器（1002 跳登录）
    ├── utils/status.js     # T1 状态 → 中文标签/颜色映射（只读，不增不删）
    ├── views/              # 13 页，按 B-Pxx 命名
    └── components/         # ProductCard / OrderStatusTag / EmptyBox 等复用组件
```

## 分层约束

1. **api/ 只发请求**：每个函数对应一个 T5 接口编号，注释标注编号与错误码；视图层不得直接 axios。
2. **stores/ 只管登录态**：token、userInfo、logout；业务数据由各 view 局部 state 管理。
3. **utils/status.js 是 T1 状态机的唯一前端映射**：状态名与中文标签一一对应，禁止在其他文件散落硬编码状态判断。
4. **router 守卫**：未登录访问需登录页 → `/login`（带 redirect 参数）；已登录访问 `/login` → 首页。

## 编码约定

- 组件名 PascalCase，文件名与路由 kebab-case；页面视图与 B-01 编号对应
- 列表页统一封装 `usePagination`：page/pageSize 初始 1/10，返回 data.list/data.total
- 提交类操作统一 loading + 成功刷新列表 + 失败 ElMessage（后端 message）
- 空状态一律 `el-empty` + 引导文案（如"购物车还是空的，去逛逛"）
- 按钮显隐规则集中注释 T1 出处：`<!-- T1: 仅 PENDING_PAY 可取消 -->`
- 不引入团队未约定的 UI 库/状态库（只用 Element Plus + Pinia + Vue Router）
