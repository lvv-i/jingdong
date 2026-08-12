# 京东风格电商平台课程作业

本项目为课程作业，目标是一套后端覆盖用户网页端、App、小程序、商家后台和管理员后台的多端电商平台。

## 项目文档

- [电商项目团队分工与公共配置方案.docx](./电商项目团队分工与公共配置方案.docx) - 成员分工、公共规范、开发计划
- [电商项目四人工分与第一阶段任务书.docx](./电商项目四人工分与第一阶段任务书.docx) - 4人分工调整与第一阶段（需求冻结）各成员任务
- [京东风格电商平台项目启动指导书_修订版.docx](./京东风格电商平台项目启动指导书_修订版.docx) - 原始启动指导书
- [作业要求.docx](./作业要求.docx) - 课程作业评分标准

## 共享配置

项目使用 Qoder 共享配置，存放在 `.qoder/` 目录：

- `.qoder/skills/` - 团队共享 skills
- `.qoder/rules/` - 团队共享 rules
- `.qoder/mcps/` - MCP 使用说明

## 技术栈

- 后端：Spring Boot + MyBatis + MySQL
- 用户网页端：Vue 3 + Vite + Element Plus
- 后台端：Vue 3 + Element Plus
- 移动端：uni-app（App + 微信小程序）

## 进度跟踪（全员必读）

项目进度由 4 个文件统一记录，**任何成员任务推进后都必须更新对应文件并随任务一起提交**，让全员随时能看到最新状态：

| 文件 | 作用 | 谁更新 |
|---|---|---|
| [PROJECT_STATUS.md](./PROJECT_STATUS.md) | 项目状态总览（当前阶段、成员摘要、里程碑、风险） | 任何成员 |
| [docs/progress/member-status.md](./docs/progress/member-status.md) | 各成员任务级完成状态 | 任务责任人 |
| [docs/progress/roadmap.md](./docs/progress/roadmap.md) | 五阶段项目进度与里程碑 | 阶段负责人 |
| [docs/progress/backlog.md](./docs/progress/backlog.md) | 待实现进度清单（接下来做什么） | 任何成员 |

**维护要求：**

1. 任务开始/完成/阻塞时，立即更新对应文件（状态图标：⬜ 未开始 ｜ 🟦 进行中 ｜ ✅ 已完成 ｜ 🚫 阻塞）。
2. 更新明细文件（member-status / roadmap / backlog）时，必须同步更新 `PROJECT_STATUS.md` 总览。
3. 进度变更与任务交付物在同一次提交中推送到 `develop`。
4. 各成员使用 AI 协作时，应让 AI 在每次任务推进后自动完成上述更新与提交。

## 协作规范

1. 使用 `main` 作为稳定分支，`develop` 作为日常集成分支。
2. 功能开发从 `develop` 切出 `feature/<模块>-<简述>` 分支。
3. 提交信息格式：`type(scope): subject`。
4. 合并到 `develop` 或 `main` 必须通过 Pull Request。

## 快速开始

各端独立工程后续接入此仓库，具体启动方式由各端负责人补充。
