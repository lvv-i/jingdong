# 京东风格电商平台课程作业

本项目为课程作业，目标是一套后端覆盖用户网页端、App、小程序、商家后台和管理员后台的多端电商平台。

## 项目文档

- [电商项目团队分工与公共配置方案.docx](./电商项目团队分工与公共配置方案.docx) - 成员分工、公共规范、开发计划
- [京东风格电商平台项目启动指导书_修订版.docx](./京东风格电商平台项目启动指导书_修订版.docx) - 原始启动指导书

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

## 协作规范

1. 使用 `main` 作为稳定分支，`develop` 作为日常集成分支。
2. 功能开发从 `develop` 切出 `feature/<模块>-<简述>` 分支。
3. 提交信息格式：`type(scope): subject`。
4. 合并到 `develop` 或 `main` 必须通过 Pull Request。

## 快速开始

各端独立工程后续接入此仓库，具体启动方式由各端负责人补充。
