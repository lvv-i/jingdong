# 成员 C 专属配置索引

> 仅成员 C（商家后台 + 管理员后台）使用。其他成员工作时不加载本目录。
> 使用方式：做每个任务前先读取 `docs/phase4/member-c/tasks/` 对应任务文档，再读取本目录关联配置。

## 配置清单

| 类型 | 文件 | 用途 | 对应第四阶段任务 |
|------|------|------|------------------|
| skill | `skills/admin-page-dev.md` | 后台页面开发步骤（列表/表单/审核流页模板） | C1/C3/C4 |
| skill | `skills/backend-contract-check.md` | 按 T5 编号核对页面↔接口↔字段映射 | C3/C4/C6 |
| rule | `rules/admin-frontend.md` | 后台工程结构、状态按钮显隐、越权提示规范 | C2/C5 |
| mcp | `mcps/browser-use-guide.md` | browser-use 逐页验证交互、genui 原型参考 | C5/C6 |

## 与团队共享配置的关系

- 团队共享 `.qoder/rules/` 对我**同样强制**（尤其 state-machine.md、api-contract.md、code-style.md、git-workflow.md）。
- 团队共享 `.qoder/skills/frontend-component-guide.md` 是组件选择基线，我的页面模板在其之上补充后台场景（审核流、状态按钮、审计留痕展示）。
- 本目录是我在团队规范之上的**个人工作方法**，只约束我自己产出的页面与文档。
- 冲突处理：团队 rules > 本目录 rules > 个人临时想法。
