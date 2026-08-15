# Rule (D专属): uni-app 工程结构与端差异适配规范

## Scope

适用于成员 D 的 `mobile-app/` uni-app 工程（Vue 3 + Vite）。一套代码输出 App + 微信小程序 + H5。

## 目录结构

```
mobile-app/
├── src/
│   ├── pages/            # 页面（与 D-01 清单一一对应）
│   │   ├── index/        # D-P01 首页（tabBar）
│   │   ├── category/     # D-P02 分类页（tabBar）
│   │   ├── product/      # D-P03 商品详情
│   │   ├── cart/         # D-P04 购物车（tabBar）
│   │   ├── checkout/     # 结算页
│   │   ├── order/        # D-P05 订单列表 + 详情
│   │   ├── notice/       # D-P06 消息页
│   │   ├── mine/         # D-P07 我的（tabBar）
│   │   ├── refund/       # D-P08 售后列表 + 发起
│   │   ├── address/      # 地址列表 + 编辑
│   │   └── login/        # D-P09 登录/注册
│   ├── components/       # 复用组件（商品卡片/订单卡片/退款卡片/空状态）
│   ├── api/              # 接口模块（按 T5 分组，函数注释标注接口编号）
│   ├── stores/           # Pinia（user/ cart）
│   ├── utils/            # request.js（请求层）、platform.js（端适配）、auth.js
│   └── static/           # 静态资源（图标、默认图）
├── pages.json            # 页面注册 + tabBar 4 项（首页/分类/购物车/我的）
├── manifest.json         # App/小程序配置
└── vite.config.js
```

## 端差异适配层约定

1. 端差异代码必须集中在 `utils/platform.js` 与条件编译块内，业务逻辑三端共享。
2. 安全区：底部操作栏 APP/H5 用 `env(safe-area-inset-bottom)`；小程序用 `safe-area-inset-bottom` 常量的 `::after` 占位。
3. 登录方式：App/小程序主推短信验证码登录（P-007/P-008）；H5 同时支持账号密码（P-002）。
4. tabBar 徽标（购物车数量/未读消息）在 H5 无原生徽标时用页面内角标兜底。

## 状态与权限约束（强制）

1. 前端只读 T1 状态机：按钮显隐只依赖后端返回的 `status` 字段，禁止前端自创状态名、禁止数字状态码。
2. 权限判断在后端完成，前端仅做按钮隐藏；未登录访问受保护页面统一跳登录（code=1002 响应兜底）。
3. 统一返回格式 `{code, message, data, total}`：只认 `code===200` 为成功，业务错误 HTTP 仍为 200。

## 禁止事项

- 禁止在页面组件里直接 `uni.request`/`fetch`（必须走 `utils/request.js`）
- 禁止把 token 硬编码进任何页面
- 禁止绕过请求层自行解析响应结构
- 禁止为某个端单独复制一份页面代码（差异必须条件编译）
- 禁止空白页：所有列表/详情页必须有加载态、空状态、错误态
