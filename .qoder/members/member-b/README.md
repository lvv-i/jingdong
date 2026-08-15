# 成员 B 专属配置索引

> 仅成员 B（用户网页端）使用。其他成员工作时不加载本目录。
> 使用方式：做每个任务前先读取 `docs/phase4/member-b/tasks/W1-W5-用户网页端任务书.md` 对应章节，再读取本目录关联配置。

## 配置清单

| 类型 | 文件 | 用途 | 对应第四阶段任务 |
|------|------|------|------------------|
| skill | `skills/vue-page-spec.md` | 按 B-01 页面清单生成/审查用户端页面 | W2-W5 |
| skill | `skills/api-integration.md` | 按 T5 契约对接接口（封装请求/错误码处理/分页） | W1-W5 |
| rule | `rules/frontend-structure.md` | 用户端工程目录结构与分层规范 | W1 及后续 |
| mcp | `mcps/browser-mcp-guide.md` | browser-use 端到端验证页面交互 | W2-W5 |

## 与团队共享配置的关系

- 团队共享 `.qoder/rules/` 对我**同样强制**（尤其 api-contract.md、code-style.md、state-machine.md）。
- 团队共享 `.qoder/skills/frontend-component-guide.md` 是我的基础组件选择依据，本目录 skills 在其之上做页面级规格化。
- 本目录是我在团队规范之上的**个人工作方法**，只约束我自己产出的页面与代码。
- 冲突处理：团队 rules > 本目录 rules > 个人临时想法。

## 契约输入（只读，不自创）

| 契约 | 文件 | 对我方的约束 |
|------|------|--------------|
| 状态机 | `docs/phase1/member-a/deliverables/01-状态机枚举表.md` | 订单 5 态 / 售后 6 态，按钮显隐只读后端状态 |
| 错误码 | `docs/phase1/member-a/deliverables/03-错误码分段表.md` | 只按 `code` 展示 `message`；1002 跳登录；2001~2004 注册/登录特殊文案 |
| 接口清单 | `docs/phase1/member-a/deliverables/05-接口清单v1.0.md` | P-001~008 + U-001~025 共 33 接口，统一返回 `{code,message,data,total}`，分页 `page/pageSize/list/total` |
| 后端实现 | `backend/`（A 已完成 67 接口） | 联调对象；M3 里程碑待 MySQL 8 环境 |
# 成员 B 专属配置索引

> 仅成员 B（用户网页端）使用。其他成员工作时不加载本目录。
> 使用方式：做每个任务前先读取 `docs/phase4/member-b/tasks/W1-W5-用户网页端任务书.md` 对应章节，再读取本目录关联配置。

## 配置清单

| 类型 | 文件 | 用途 | 对应第四阶段任务 |
|------|------|------|------------------|
| skill | `skills/vue-page-spec.md` | 按 B-01 页面清单生成/审查用户端页面 | W2-W5 |
| skill | `skills/api-integration.md` | 按 T5 契约对接接口（请求封装/错误码处理/分页） | W1-W5 |
| rule | `rules/frontend-structure.md` | 用户端工程目录结构与分层规范 | W1 及后续 |
| mcp | `mcps/browser-mcp-guide.md` | browser-use 端到端验证页面交互 | W2-W5 |

## 与团队共享配置的关系

- 团队共享 `.qoder/rules/` 对我**同样强制**（尤其 api-contract.md、code-style.md）。
- 本目录是我在团队规范之上的**个人工作方法**，只约束我自己产出的页面与代码。
- 冲突处理：团队 rules > 本目录 rules > 个人临时想法。
