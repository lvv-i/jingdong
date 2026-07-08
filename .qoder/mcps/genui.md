# MCP: genui 使用说明

## 用途

genui MCP 用于在聊天窗口中渲染交互式 HTML 组件。适合快速制作页面原型、状态机流程图、数据看板、表单Demo或答辩演示用的小工具。

## 适用场景

- 快速绘制订单状态机流转图，供团队确认。
- 制作商品列表/订单列表的低保真页面原型。
- 展示核心业务流程的交互Demo。
- 在需求评审会上用可视化组件统一团队理解。

## 已配置工具

| 工具 | 用途 |
|------|------|
| `load_guidelines` | 首次使用 show_widget 前必须调用，加载设计规范 |
| `show_widget` | 渲染 HTML 组件，支持 inline 代码或 template 模板 |

## 使用规范

### 1. 首次使用必须先加载规范

调用 `load_guidelines`，根据要创建的组件类型选择模块：

- `interactive`：交互式组件
- `chart`：图表
- `diagram`：流程图、状态机图
- `mockup`：页面原型
- `form`：表单

示例：

```json
{
  "modules": ["diagram", "mockup"]
}
```

### 2. 渲染组件

使用 `show_widget` 时需注意：

- `i_have_seen_guidelines` 必须设置为 `true`。
- inline 模式下，`widget_code` 只能使用 HTML 片段，不能包含 `<!DOCTYPE>`、`<html>`、`<head>`、`<body>`。
- 代码顺序：`<style>` → HTML 内容 → `<script>`。
- 颜色只能使用 CSS 变量。
- 仅可使用以下 CDN：cdnjs.cloudflare.com、cdn.jsdelivr.net、unpkg.com、esm.sh。
- 不要使用 `on*` 事件属性选择元素，使用 class、id 或 data-* 属性。

### 3. 更新模式

- `replace`（默认）：用新代码替换整个组件。
- `patch`：针对现有组件做局部替换，适合只改数据或文本时保留状态。

## 使用示例

### 渲染订单状态机图

```json
{
  "title": "order_state_machine",
  "i_have_seen_guidelines": true,
  "widget_code": "<style>:root{--node-bg:#e6f7ff;--arrow:#1890ff;--text:#333;} .node{padding:8px 16px;background:var(--node-bg);border-radius:4px;display:inline-block;margin:8px;} .arrow{color:var(--arrow);margin:0 8px;}</style><div style='text-align:center;'><span class='node'>待支付</span><span class='arrow'>→</span><span class='node'>已支付待发货</span><span class='arrow'>→</span><span class='node'>已发货待收货</span><span class='arrow'>→</span><span class='node'>已完成</span></div>",
  "update_mode": "replace"
}
```

### 使用模板文件

如果团队有重复的组件需求，可以预先把 HTML 模板放到项目目录，例如 `.qoder/widgets/order-state-machine.html`，然后通过 `widget_path` 引用：

```json
{
  "title": "order_state_machine",
  "i_have_seen_guidelines": true,
  "widget_path": ".qoder/widgets/order-state-machine.html",
  "data": {
    "currentState": "PAID"
  },
  "update_mode": "replace"
}
```

模板中通过 `window.__WIDGET_DATA__` 读取传入的数据。

## 团队协作约定

1. 可复用的 widget 模板由成员E收集整理，存放在 `.qoder/widgets/` 目录。
2. 状态机图、核心流程图等关键可视化组件应在需求冻结阶段完成，作为团队共识依据。
3. 答辩前可生成核心页面的 mockup 组件，辅助讲解系统设计。

## 注意事项

- `load_guidelines` 只需在首次渲染前调用一次，无需重复调用。
- 复杂组件优先使用 `widget_path` + 模板文件，避免每次生成大量 inline HTML。
- 不要在 widget 中引入外部未授权的脚本或样式。
