# Skill (D专属): 移动端接口对接（T5 契约）

## Description

指导成员 D 按 T5 接口清单 v1.0 封装 uni-app 请求层与接口模块，统一处理鉴权头、统一返回格式、错误码与分页，保证移动端与网页端/后台共用同一套契约。

## When to Use

- 第四阶段 X1-X5：每个页面开发前确认所用接口编号与契约
- 联调排障：接口返回与预期不符时对照 T5 核对

## 请求层封装要点（utils/request.js）

1. baseURL：联调环境 `http://localhost:8080`（D-02 约定），App/小程序真机联调可用环境变量覆盖
2. 请求拦截器：注入 `Authorization: Bearer <token>`（白名单接口除外：P-001~P-008）
3. 响应拦截器统一处理：
   - `code === 200`：返回 `data`
   - `code === 1002`：清 token 跳登录页，toast「请先登录」
   - 其他：toast 展示 `message`，Promise reject 供页面 catch
4. 分页统一：参数 `page/pageSize`，读取 `data.list` 与 `data.total`

## 接口模块组织（api/）

按 T5 分组建模块：`api/product.js`（P-003~P-006）、`api/user.js`（P-001/P-002/P-007/P-008、U-001/U-002）、`api/cart.js`（U-008~U-011）、`api/order.js`（U-012~U-017）、`api/refund.js`（U-018~U-021）、`api/address.js`（U-003~U-007）、`api/notice.js`（U-022~U-025）。

每个函数注释标注 T5 编号与主要错误码，例如：

```js
// U-015 模拟支付 PENDING_PAY → PAID；错误码 4001/4002/3003/4007/4008
export const payOrder = (id) => request.post(`/orders/${id}/pay`)
```

## 错误码处理速查（移动端高频）

| code | 场景 | 移动端处理 |
|------|------|-----------|
| 1002 | 未登录 | 跳登录页，登录后回跳原页 |
| 3002 | 商品非 ON_SALE | 提示「商品已下架」，返回上一页 |
| 4006 | 库存不足 | 结算页提示并刷新商品数据 |
| 4007 | 价格已变化 | 提示刷新，重新拉取购物车/结算数据 |
| 4008 | 重复支付 | 提示「请勿重复支付」，跳订单详情 |
| 5005 | 售后状态不符 | 刷新售后列表 |
| 1001 | 参数/状态非法 | 直接展示 message |

## 审查清单

1. 每个接口调用对应 T5 编号，参数名与契约一致
2. 无绕过请求层直接 fetch/uni.request 的散装调用
3. 1002 统一跳登录，无页面级重复实现
4. 提交类接口有 loading 与防重复提交
5. 列表分页参数与 data.list/total 读取正确
