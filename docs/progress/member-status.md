# 各成员完成状态

> 本文件记录每个成员的任务级完成状态，是所有成员互看进度的唯一明细来源。
> 关联：[项目状态总览](../../PROJECT_STATUS.md) ｜ [项目进度](roadmap.md) ｜ [待实现进度](backlog.md)

## 状态图例

- ⬜ 未开始 ｜ 🟦 进行中 ｜ ✅ 已完成 ｜ 🚫 阻塞

---

## 成员 A（后端核心）

第一阶段任务（工作区：`docs/phase1/member-a/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| T1 | 状态机枚举表（全员签字版） | ✅ | `docs/phase1/member-a/deliverables/01-状态机枚举表.md`（v1.0，B/C/D 已签字） | 2026-08-12 |
| T2 | 数据字典 v1.0 | ✅ | `docs/phase1/member-a/deliverables/02-数据字典v1.0.md`（13 张表 + 评价字段扩展） | 2026-08-12 |
| T3 | 错误码分段表 | ✅ | `docs/phase1/member-a/deliverables/03-错误码分段表.md`（7 段 + 公共码） | 2026-08-12 |
| T4 | JWT 鉴权与 RBAC 方案 | ✅ | `docs/phase1/member-a/deliverables/04-JWT鉴权与RBAC方案.md`（三角色 + 拦截矩阵） | 2026-08-12 |
| T5 | 接口清单 v1.0 | ✅ | `docs/phase1/member-a/deliverables/05-接口清单v1.0.md`（v1.0 定稿 67 接口，已合并 B/C/D 需求） | 2026-08-12 |
| T6 | 需求冻结定稿、签字与交接 | ✅ | `docs/phase1/member-a/deliverables/06-需求冻结A部分定稿.md` | 2026-08-12 |

**A 完成率：6 / 6（100%）✅ 第一阶段全部完成**

第三阶段任务（工作区：`backend/`、`docs/phase3/member-a/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| S1 | 建库建表与种子数据 | ✅ | `backend/sql/20260812_001_schema.sql`（13 表）+ `002_seed.sql`（5 账号 + 演示数据） | 2026-08-15 |
| S2 | 工程初始化 | ✅ | `backend/pom.xml`（Boot 3.2.5 + MP + JJWT）+ 公共层/安全层/四枚举 | 2026-08-15 |
| S3 | 接口开发 67 个 | ✅ | 19 Controller（P-001~008 / U-001~025 / M-001~015 / A-001~019），`mvn compile` 通过 | 2026-08-15 |

**A 第三阶段完成率：3 / 3（100%）✅，任务书见 `docs/phase3/member-a/tasks/S1-S3-数据库与后端任务书.md`**

第四阶段任务（工作区：`docs/phase4/member-a/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| M3 | MySQL 8 部署与后端联调验证（里程碑） | ✅ | `docs/phase4/member-a/deliverables/M3-联调验证记录.md`（MySQL 8.0.28 部署 + 20 步全链路联调 + 17 项抽测全部 code=200 + 浏览器端到端截图 2 张） | 2026-08-15 |
| 状态机缺口修复 | M-002b 店铺重提 + M-006 扩展商品重提（C 联调反馈） | ✅ | 新增 `POST /api/merchant/shop/resubmit`（REJECTED→PENDING_AUDIT，1001 拦截 + RESUBMIT 审计）；M-006 扩展 DRAFT/OFF_SALE→PENDING_ON_SALE；正反 7 用例实测通过 | 2026-08-29 |
| T5 v1.1 | 接口清单修订（U-012/P-008/U-024/M-002b/M-006） | ✅ | `docs/phase1/member-a/deliverables/05-接口清单v1.0.md`（v1.1，68 接口，版本记录 6 项修订） | 2026-08-29 |
| H2 重启幂等 | 重启报错修复 | ✅ | `h2-schema.sql` 全改 IF NOT EXISTS（44 处）+ 本地配置 `sql.init.mode=never`；重启验证种子账号 0 重置、数据完整 | 2026-08-29 |
| AI 交互记录 | 归档（作业「交互过程 30 分」备料） | ✅ | `docs/phase4/member-a/deliverables/A6-AI交互记录.md` | 2026-08-29 |

**A 第四阶段完成率：1 / 1（100%）✅（M3 里程碑达成，B/C/D 可切换真实后端联调）+ 第五阶段收尾（2026-08-29：2 个状态机缺口修复 + T5 v1.1 + H2 幂等修复 + AI 交互记录归档）**

第五阶段任务（工作区：`backend/`、`docs/phase1/member-a/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| A-缺口 | 状态机重提缺口修复（T1 3.2 店铺 REJECTED→PENDING_AUDIT / 4.2 商品 OFF_SALE→PENDING_ON_SALE） | ✅ | `MerchantServiceImpl.java`（updateShop/submitProduct）；状态机重提回归脚本 21/21 PASS | 2026-08-29 |
| A-ResubmitFix | resubmitShop auditReason 置空失效修复（C 端 E2E 实测发现） | ✅ | `MerchantServiceImpl.java`（updateById NOT_NULL 策略跳过 null → 改 LambdaUpdateWrapper 显式 set audit_reason=null）；MySQL 真实环境 9 步 E2E 实测全通过 | 2026-08-29 |
| A-U024 | 评价高缺陷配套：OrderListItemVO/OrderDetailVO 新增 reviewed 字段 | ✅ | `vo/OrderListItemVO.java` + `vo/OrderDetailVO.java`；U-024 回归脚本 18/18 PASS | 2026-08-29 |
| A-T5 | T5 接口清单校准 6 处（U-002/U-004/U-010/U-012 + P-008/U-024 幽灵码 + M-002/M-006 REJECTED 重提），版本记录追加 v1.1 | ✅ | `docs/phase1/member-a/deliverables/05-接口清单v1.0.md`（v1.1 变更记录行） | 2026-08-29 |
| A-T1v1.2 | T1 3.2「修改资料后重新提交」语义完善：M-002b 支持可选请求体（ShopResubmitDTO 全可选，携带字段先更新再重提，空 body 向后兼容，3004 校验 + 审计 remark 区分） | ✅ | `dto/ShopResubmitDTO.java`（新建）+ `service/MerchantService.java` + `service/impl/MerchantServiceImpl.java` + `controller/merchant/MerchantController.java`；MySQL 真实环境 16 步 E2E 全绿 + 中文写入 SQL 字节级验证；修复 shop2 双重编码历史数据 | 2026-08-29 |
| A-T5v1.2 | T5 接口清单升版 v1.2（M-002b 请求参数改为可选 {shopName?, categoryId?, description?} + 错误码 3004） | ✅ | `docs/phase1/member-a/deliverables/05-接口清单v1.0.md`（v1.2 版本记录行） | 2026-08-29 |
| A-Deploy | 答辩环境加固：一键启动脚本 + SPA 静态托管 | ✅ | `start-all.bat`（MySQL → 后端 jar → 用户端 5173 → 后台 5174 → 移动 H5 五步拉起 + 停止清理）+ `config/SpaFallbackController.java`（新建）+ `config/WebConfig.java`（静态资源映射）+ README 启动指南；jar 重建运行验证 | 2026-08-30 |

**A 第五阶段完成率：7 / 7（100%）✅（2026-08-30 全部闭环）**

---

## 成员 B（用户网页端）

第一阶段任务（工作区：`docs/phase1/member-b/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| B1 | 用户端页面清单 | ✅ | `docs/phase1/member-b/deliverables/B-01-用户端页面清单.md`（13 页） | 2026-08-12 |
| B2 | 用户端接口需求 | ✅ | `docs/phase1/member-b/deliverables/B-02-用户端接口需求清单.md`（4 项增补） | 2026-08-12 |

**B 完成率：2 / 2（100%）✅**

第四阶段任务（工作区：`docs/phase4/member-b/`、工程 `frontend-user/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| W1 | 工程初始化与登录注册（P-001/002/007/008） | ✅ | `docs/phase4/member-b/deliverables/W-01-工程初始化.md`（frontend-user 工程 + 13 页路由骨架 + 双登录） | 2026-08-15 |
| W2 | 商品浏览链路（P-003~006：首页/搜索/详情/评价） | ✅ | `docs/phase4/member-b/deliverables/W-02-商品浏览链路.md`（Home/Search/ProductDetail + ProductCard 组件 + 加购 U-009） | 2026-08-15 |
| W3 | 购物与下单链路（U-008~016：购物车/结算/下单/支付） | ✅ | `docs/phase4/member-b/deliverables/W-03-购物与下单链路.md`（购物车/结算/订单详情三页 + 4001~4008 分支，browser-use 实测） | 2026-08-15 |
| W4 | 订单与售后链路（U-013~021/024：订单中心/售后/评价） | ✅ | `docs/phase4/member-b/deliverables/W-04-订单与售后链路.md`（订单中心六 Tab + 售后中心七 Tab + 评价页，browser-use 实测三页） | 2026-08-15 |
| W5 | 个人中心链路与全链路自测（U-001~007/022~025） | ✅ | `docs/phase4/member-b/deliverables/W-05-个人中心与自测.md`（个人中心/地址/通知三页 + 13 页空状态核查 + browser-use 实测） | 2026-08-15 |
| W6 | 接口契约核对与注释对齐（补充轮，不依赖 M3） | ✅ | `docs/phase4/member-b/deliverables/W-06-接口契约核对表.md`（33 接口逐条核对 ✅⚠️🔴）+ api 注释修正（auth/refund/order）+ 偏差反馈清单 | 2026-08-29 |
| M3 | 真实后端联调（baseURL→8080 + 双兼容 5 处） | ✅ | `docs/phase4/member-b/deliverables/B6-AI交互记录.md` + 截图 5 张（m3-home/m3-login/m3-product/m3-cart/m3-orders）；14 接口全链路实测全通过 + 5 页页面级实测真实数据渲染 | 2026-08-29 |
| W7 | M3 真实联调（后端 H2 上全量验证 + 记录归档） | ✅ | `docs/phase4/member-b/deliverables/W-07-真实联调记录.md`（P/U 全验证 + 跨角色闭环 + 契约细节校准）+ `W-08-演示脚本与截图说明.md`（用户侧五场景） | 2026-08-29 |
| W8 | M4 前中低缺陷批量修复 | ✅ | `Login.vue`（移除 2005 幽灵码分支）+ `App.vue`（购物车徽标真实角标）+ `Checkout.vue`/`OrderDetail.vue`（支付/下单错误静默兜底）+ `api/auth.js`（silent 透传）；`npm run build` 通过 | 2026-08-29 |

**B 第四阶段完成率：5 / 5（100%）✅ + 补充 W6/W7 + M3 页面级联调（2026-08-29 全部闭环）。任务书见 `docs/phase4/member-b/tasks/W1-W5-用户网页端任务书.md`（2026-08-15 W1~W5 全部完成：浏览→搜索→详情 + 购物车→结算→下单→模拟支付 + 订单中心六 Tab/确认收货/评价入口/退款入口 + 售后中心七 Tab + 评价页 + 个人中心/地址/通知；13 页空状态与错误兜底逐页核查全通过（P1 无空白）；build 通过 + browser-use 实测。W6（2026-08-29）：契约核对表 33 接口逐条核对，B 端消费与后端实现一致（U-003/U-008 裸数组、U-012 data.orders），0 功能改动，修正 3 处注释漂移，偏差清单已反馈 A 并于 T5 v1.1 闭环。W7（2026-08-29）：真实联调 47 用例全过——W-06 待验证错误码 2003/3001/3002/4001/4002/4003/4005/5003/5005/1004 全实测闭环，U-021 商家拒绝后介入路径实测；契约实况校准 3 条（U-012 orderId 字段名、isDefault/selected 整数 0/1、U-018 无 refundNo）。M3 页面级（2026-08-29）：双兼容 5 处合入 + 14 接口全链路实测 + 5 页页面级实测 + 截图 5 张归档）**

---

## 成员 C（商家后台 + 管理员后台）

第一阶段任务（工作区：`docs/phase1/member-c/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| C1 | 后台页面清单 | ✅ | `docs/phase1/member-c/deliverables/C-01-后台页面清单.md`（6+7 页） | 2026-08-12 |
| C2 | 权限矩阵与接口需求 | ✅ | `docs/phase1/member-c/deliverables/C-02-权限矩阵.md` + `C-03-后台接口需求清单.md` | 2026-08-12 |

**C 完成率：2 / 2（100%）✅**

第四阶段任务（工作区：`docs/phase4/member-c/`、工程 `admin-web/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| C1 | 工程初始化（Vite + Vue3 + Element Plus + 路由/状态/请求基建） | ✅ | `admin-web/`（Vite 5.4 + Vue 3.5 + EP 2.8，build 通过） | 2026-08-15 |
| C2 | 登录与角色化布局（登录页/路由守卫/动态菜单/Token 拦截） | ✅ | `admin-web/src/`（P-002 登录页 + 守卫 + 13 页骨架 + T5 全量接口封装，browser-use 实测） | 2026-08-15 |
| C3 | 商家后台 6 页（仪表盘/商品/库存/订单/售后/店铺，M-001~015） | ✅ | `admin-web/src/views/merchant/` 6 页 + `deliverables/C3-接口核对表.md`（15/15，browser-use 实测） | 2026-08-15 |
| C4 | 管理员后台 7 页（审核/类目/巡检/商家/用户/争议/日志统计，A-001~019） | ✅ | `admin-web/src/views/admin/` 7 页 + `deliverables/C4-接口核对表.md`（18/19 调用，A-013 暂不调用；browser-use 实测 7 页） | 2026-08-15 |
| C5 | 状态机与权限联动（按钮显隐/越权提示/三方协作链路自测） | ✅ | `deliverables/C5-状态按钮映射表.md`（13 页全覆盖）+ `C5-交互验收清单.md`（6 用例 browser-use 实测，0 遗留） | 2026-08-15 |
| C6 | 联调与验收（契约核对/空状态/演示脚本/AI 交互记录归档） | ✅ | `deliverables/C6-契约核对表.md`（34 接口汇总）+ `C6-演示脚本与截图说明.md` + `C6-AI交互记录.md`（2026-08-29 M3 联调完成） | 2026-08-15 |
| C7 | M4 前收尾：构建复核 + dev 端口对齐 | ✅ | `npm install` + `npm run build` 通过；`vite.config.js` dev 端口统一 5174（与 C5 交互验收实测一致），C1/X-09 端口描述同步修正 | 2026-08-29 |
| C8 | 店铺重提 M-002b 接入 + T1 3.2 表单联动（补 T1 3.2 C 端缺口） | ✅ | `api/merchant.js` 新增 M002b_resubmitShop（恒传 `data || {}`）+ `views/merchant/Shop.vue` REJECTED 改“修改资料并重新提交”按钮（openResubmit 预填表单，handleSave 分流：resubmit 带资料走 M-002b / edit 走 M-002，弹窗文案随模式切换）；`npm run build` 通过；配合 A 完成 MySQL 真实环境 9 步 + 16 步 E2E 实测闭环 | 2026-08-29 |

**C 第四阶段完成率：6 / 6（100%）✅ 全部完成 + C7/C8 收尾（2026-08-29）。任务书见 `docs/phase4/member-c/tasks/`（2026-08-15 C1-C6 完成：商家后台 6 页接通 M-001~015、管理员后台 7 页接通 A-001~019、13 页状态按钮映射与越权拦截核查通过、34 接口汇总核对 + 演示脚本 + AI 记录归档，browser-use 实测；**M3 联调完成（2026-08-29，H2 备选环境：登录 / 错误码 1002/1003 实测 / 四条链路流转 / 审计日志 6 条留痕全通过，模糊点 1 闭环；发现 2 个后端状态机缺口已反馈 A，2026-08-29 由 A 修复并回归 21/21 闭环，详见 `deliverables/C6-契约核对表.md` 第 5 节）；C8（2026-08-29）店铺重提 M-002b 接入闭环，MySQL 真实环境 9 步 E2E 实测全通过**）**

---

## 成员 D（App / 小程序 + 测试文档）

第一阶段任务（工作区：`docs/phase1/member-d/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| D1 | 移动端页面清单 | ✅ | `docs/phase1/member-d/deliverables/D-01-移动端页面清单.md`（9 页） | 2026-08-12 |
| D2 | 测试基线文档 | ✅ | `docs/phase1/member-d/deliverables/D-02-测试账号与演示数据方案.md` | 2026-08-12 |
| D3 | 汇总需求冻结文档 | ✅ | `docs/phase1/member-d/deliverables/D-03-需求冻结文档v1.0.md`（全员签字） | 2026-08-12 |

**D 完成率：3 / 3（100%）✅**

第四阶段任务（工作区：`docs/phase4/member-d/`）：

| 任务 | 内容 | 状态 | 交付物 | 完成日期 |
|---|---|---|---|---|
| X1 | 工程初始化 + 登录注册（P-001/P-002/P-007/P-008） | ✅ | `docs/phase4/member-d/deliverables/X-01-工程初始化.md`（mobile-app uni-app CLI 工程 + 双登录 + 守卫，browser-use 实测） | 2026-08-15 |
| X2 | 商品浏览链路（P-003~P-006） | ✅ | `docs/phase4/member-d/deliverables/X-02-商品浏览链路.md`（首页/分类/详情三页 + mock 数据 browser-use 实测 6 场景） | 2026-08-15 |
| X3 | 购物与下单链路（U-003/U-008~U-016） | ✅ | `docs/phase4/member-d/deliverables/X-03-购物与下单链路.md`（购物车/结算/支付三页 + mock 状态机 browser-use 实测 13 场景） | 2026-08-15 |
| X4 | 订单与售后链路（U-013~U-025） | ✅ | `docs/phase4/member-d/deliverables/X-04-订单与售后链路.md`（订单列表 6 Tab + 详情 5 态操作栏 + 售后 6 态流转 + 评价 1-5 星 + 消息已读/全部已读，build 通过 + browser-use 实测） | 2026-08-15 |
| X5 | 我的/地址 + 端适配 + 全链路自测（U-001~U-007） | ✅ | `docs/phase4/member-d/deliverables/X-05-端适配与自测.md`（我的页功能入口 + 个人资料 + 地址增删改默认 + 端适配收尾 + 17 页全链路 15 步自测 + 空状态 9 项巡检全通过） | 2026-08-15 |
| X6 | 接口契约核对与功能修复（补充轮，不依赖 M3） | ✅ | `docs/phase4/member-d/deliverables/X-06-接口契约核对表.md`（33 接口逐条核对）+ 6 处运行时修复（U-003/U-008×4 列表解包、U-012 错误分支、U-002 改密字段）+ 4 处注释修正 + 偏差反馈清单 | 2026-08-29 |
| X7 | M3 真实联调验证（真实 token 联调 + 全链路闭环 + 联调记录归档） | ✅ | `docs/phase4/member-d/deliverables/X-07-M3联调验证记录.md`（25 步接口级全链路闭环 + H5 页面级实测 + 发现修复 H2 中文乱码配置缺陷 + 2 项契约观察项）。早期会话过程记录：`X-07-移动端测试用例清单.md`（46 用例基线） | 2026-08-29 |
| X8 | 测试基线复核 + 移动端测试用例清单（② 第五阶段备料） | ✅ | `docs/phase4/member-d/deliverables/X-08-测试基线复核与移动端测试用例清单.md`（seed 与 D-02 基线 9 项全一致 + TC-01~TC-10 共 45 条用例）。早期会话过程记录：`X-08-联调回归记录.md` | 2026-08-29 |
| X9 | 联调自测脚本备料（③ 第五阶段备料） | ✅ | `docs/phase4/member-d/deliverables/X-09-联调自测脚本备料.md`（25 步接口级自测 + 5 步页面级自测 + 三端闭环 10 分钟演示脚本 + 截图归档清单）。早期会话过程记录：`X-09-演示脚本与截图说明.md` | 2026-08-29 |
| 小程序验收 | 产物重新构建 + 结构验收 | ✅ | `dist/build/mp-weixin`（2026-08-29 重新构建含双兼容修复；app.json 17 页 + tabBar 4 项 + 组件/接口层齐全）+ 截图 3 张（x9-index/x9-cart/x9-mine）+ `D6-AI交互记录.md`；GUI 验收待微信开发者工具安装后执行 | 2026-08-29 |
| X10 | M4 前 U-024 评价/地址高缺陷修复 | ✅ | `review.vue`（prepareReview 自动取首个未评价明细 + 非 COMPLETED/已评价阻断）+ `detail.vue`（onShow 刷新评价态）+ `address.vue`（编辑回显 jd_edit_addr）；`build:h5` 通过 | 2026-08-29 |
| X11 | 京冬化命名与产物重建（答辩收尾） | ✅ | `App.vue`/`manifest.json`/`pages.json` 京冬化命名规避品牌 + `rebuild-mp.bat` 重建脚本 + `build:mp-weixin`/`build:h5` 重建通过；种子图片 placehold.co 替换 | 2026-08-30 |

**D 第四阶段完成率：5 / 5（100%）✅ + 补充 X6（2026-08-29 契约对齐）+ M3 收尾 X7/X8/X9（2026-08-29 联调验收）。任务书见 `docs/phase4/member-d/tasks/X1-X5-移动端开发任务书.md`（2026-08-15 X1~X5 全部完成：17 页注册（覆盖 D-01 全部页面清单），订单 6 Tab + T1 按钮显隐、售后 6 态流转闭环、消息已读/全部已读、地址增删改查设默认、端差异适配收尾（platform.js 徽标/安全区/条件编译清单）、build:h5 通过、browser-use 实测（XHR mock 全链路 15 步）。X6（2026-08-29）：修复 6 处契约运行时代码（U-003/U-008 裸数组解包×4、U-012 错误分支重映射、U-002 newPassword 字段）+ 注释修正 4 处；`build:mp-weixin` 复核通过。X7（2026-08-29）：M3 真实联调完成——25 步接口级全链路闭环（登录/1002/1003/加购/裸数组验证/下单/支付/4008/发货/收货/评价/退款/拒绝/介入/裁决/审计日志/3001/4001/5003）、H5 browser-use 页面级 5 页实测通过、发现并修复 H2 中文乱码配置缺陷（application-h2.yml 加 encoding: UTF-8 + 重建数据验证）；X8/X9（2026-08-29）：测试基线复核与 45 条测试用例清单、联调自测脚本备料完成。本会话早期并行产出的 X-07/08/09 过程记录版（用例基线/回归记录/演示脚本）保留归档）**

---

## 更新约定

1. **谁更新**：任务状态变化时，由任务责任人更新本文件自己名下的表格。
2. **必须同步**：更新后同步修改根目录 `PROJECT_STATUS.md` 的「成员状态摘要」。
3. **如何更新**：改状态图标、填完成日期、更新完成率数字。
4. **提交**：进度变更随任务交付物一起提交到 `develop`。
