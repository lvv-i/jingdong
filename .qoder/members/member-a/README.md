# 成员 A 专属配置索引

> 仅成员 A（后端核心）使用。其他成员工作时不加载本目录。
> 使用方式：做每个任务前先读取 `docs/phase1/member-a/tasks/` 对应任务文档，再读取本目录关联配置。

## 配置清单

| 类型 | 文件 | 用途 | 对应第一阶段任务 | 后续阶段用途 |
|------|------|------|------------------|--------------|
| skill | `skills/state-machine-spec.md` | 生成/审查状态机枚举表 | T1 | 后端状态机实现 |
| skill | `skills/db-dict-generator.md` | 生成/审查数据字典 | T2 | 建表 SQL 生成 |
| skill | `skills/api-inventory.md` | 生成/审查接口清单 | T5 | Controller 实现 |
| rule | `rules/backend-layering.md` | 后端包结构与分层规范 | T4 铺垫 | 第二阶段工程骨架 |
| rule | `rules/jwt-rbac.md` | JWT 鉴权与 RBAC 实现约束 | T4 | 第二阶段鉴权实现 |
| mcp | `mcps/backend-mcp-guide.md` | genui 画状态机图 / browser-use 验证接口 | T1、T5 | 各阶段验收 |

## 与团队共享配置的关系

- 团队共享 `.qoder/rules/` 对我**同样强制**（尤其 state-machine.md、db-conventions.md、api-contract.md）。
- 本目录是我在团队规范之上的**个人工作方法**，只约束我自己产出的文档与代码。
- 冲突处理：团队 rules > 本目录 rules > 个人临时想法。
