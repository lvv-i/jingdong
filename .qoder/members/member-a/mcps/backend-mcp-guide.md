# MCP (A专属): 后端工作 MCP 使用指南

> 团队已配置 2 个 MCP（browser-use、genui），本文件说明成员 A（后端）如何利用它们辅助第一阶段与后续阶段工作。

## 1. genui —— 状态机图与文档可视化

适用任务：T1（状态机枚举表）、T5（接口清单评审）

- **画状态机流转图**：调用 `load_guidelines`（模块选 `diagram`）后，用 `show_widget` 渲染 4 类状态机流转图，与交付物中的 mermaid 图交叉核对。
- **数据字典看板**：可选，用 `mockup`/`form` 模块渲染表结构原型，辅助团队评审。
- 用法细节见 `.qoder/mcps/genui.md`（团队共享说明）。

## 2. browser-use —— 接口行为验证

适用任务：T5 之后接口联调、第二阶段后端自测

- **验证接口链路**：启动后端后，用浏览器访问前端页面或直接通过 `evaluate_script` 发起 fetch 请求，检查接口返回。
- **检查网络请求**：`list_network_requests` 查看实际请求/响应，核对统一返回格式与错误码是否符合 T3/T5 契约。
- **验证越权**：用不同角色账号（user001/merchant001/admin001）执行同一操作，确认后端拦截器按 T4 矩阵拒绝越权（如：用户访问 /api/admin/** 应返回 1003）。
- 用法细节见 `.qoder/mcps/browser-use.md`（团队共享说明）。

## A 的使用约定

1. MCP 服务端由团队统一配置，A 不重复安装。
2. 状态机图等可视化产物如要留存，截图或 HTML 模板存 `docs/phase1/member-a/deliverables/` 同目录（文件名带 `-fig` 后缀）。
3. 用 browser-use 验证接口时使用 D 提供的固定测试账号，不得临时改数据破坏演示基线。
