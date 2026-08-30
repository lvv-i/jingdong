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

### 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 17+ | 后端编译与运行 |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+ | 前端工程（含 npm） |
| MySQL | 8.0+ | 数据库（可选，H2 内嵌模式也可运行） |

### 一键启动（推荐）

```bash
# Windows：双击运行
start-all.bat

# 脚本自动启动：MySQL → 后端 8080 → 用户端 5173 → 后台端 5174 → 移动端 H5
```

### 分步启动

```bash
# 1. 数据库（二选一）
#   方案 A：MySQL 8（真实环境）
D:\mysql-8.0.28-winx64\bin\mysqld.exe
#   方案 B：H2 内嵌（后端自动创建，无需单独启动）

# 2. 后端（Spring Boot :8080）
cd backend
mvn spring-boot:run
# 或 java -jar target/jd-shop-1.0.0.jar --spring.profiles.active=h2

# 3. 用户网页端（Vue 3 :5173）
cd frontend-user
npm run dev

# 4. 后台端（Vue 3 :5174，商家+管理员共用）
cd admin-web
npm run dev

# 5. 移动端 H5（uni-app）
cd mobile-app
npm run dev:h5
```

### 访问地址

| 端 | 地址 | 测试账号 |
|---|---|---|
| 用户网页端 | http://localhost:5173 | user001 / user001 |
| 商家后台 | http://localhost:5174 | merchant001 / merchant001 |
| 管理员后台 | http://localhost:5174 | admin001 / admin001 |
| 后端 API | http://localhost:8080 | - |
| 移动端 H5 | http://localhost:端口（见终端输出） | 同上 |

### 构建生产版本

```bash
# 后端
cd backend && mvn package -DskipTests

# 用户网页端
cd frontend-user && npm run build

# 后台端
cd admin-web && npm run build

# 移动端 H5 + 微信小程序
cd mobile-app && npm run build:h5 && npm run build:mp-weixin
```
