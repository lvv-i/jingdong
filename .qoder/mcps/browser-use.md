# MCP: browser-use 使用说明

## 用途

browser-use MCP 用于在项目开发后期对网页端、商家后台、管理员后台进行端到端验证。可以模拟用户点击、填写表单、截图留档、检查网络请求和控制台日志。

## 适用场景

- 验证用户从登录到下单的完整链路。
- 验证商家后台发货、退款处理流程。
- 验证管理员审核、介入售后流程。
- 截图保存关键页面，用于答辩演示材料。
- 检查页面报错或接口异常。

## 已配置工具

该 MCP 已集成 16 个工具，常用工具如下：

| 工具 | 用途 |
|------|------|
| `navigate_page` | 跳转到指定 URL，支持 url / back / forward / reload |
| `click` | 点击页面元素 |
| `fill` | 在输入框中填写内容 |
| `hover` | 悬停在元素上 |
| `take_screenshot` | 截图保存为 png/jpeg，支持 fullPage |
| `evaluate_script` | 在页面中执行 JavaScript，用于检查状态或获取数据 |
| `list_network_requests` | 查看页面网络请求 |
| `list_console_messages` | 查看浏览器控制台日志 |
| `wait_for` | 等待某个条件或固定时间 |

## 使用示例

### 验证用户登录并下单

1. 启动本地开发服务器（用户网页端）。
2. 使用 browser-use 导航到登录页：
   ```json
   { "type": "url", "url": "http://localhost:5173/login" }
   ```
3. 填写账号密码并点击登录。
4. 导航到商品详情页，点击"加入购物车"。
5. 进入购物车，选择商品，点击"去结算"。
6. 提交订单，截图保存订单状态。
7. 使用 `evaluate_script` 检查页面显示的状态文本是否为"待支付"。

### 截图留档

```json
{
  "filePath": "d:\\Desktop\\jingdong\\screenshots\\user-order-list.png",
  "format": "png",
  "fullPage": true
}
```

截图建议保存到项目 `screenshots/` 目录，便于统一管理和答辩使用。

## 团队协作约定

1. 端到端验证脚本由成员E统一维护，放在项目 `e2e/` 或 `tests/e2e/` 目录。
2. 每次 major 功能合并到 `develop` 后，至少执行一次核心链路验证。
3. 发现的问题必须截图并附带 `list_console_messages` 和 `list_network_requests` 输出，提交到 issue 或 PR 评论。
4. 答辩前一周，使用 browser-use 跑通所有 P0 验收项，生成截图合集。

## 注意事项

- browser-use 操作的是真实浏览器环境，确保本地服务已启动。
- 测试账号使用固定账号，避免演示数据被意外修改。
- 截图文件路径使用绝对路径，避免跨机器路径不一致问题。
