# skill: backend-contract-check（页面↔接口契约核对）

> 成员 C 专属 ｜ 用于 C3/C4/C6：按 T5 编号核对页面调用与后端契约的一致性
> 基线：`docs/phase1/member-a/deliverables/05-接口清单v1.0.md`（v1.0 定稿 67 接口）

## 适用场景

- 页面开发完成后的接口调用核对（C3/C4 验收）
- 联调阶段的契约差异排查（C6）

## 核对步骤

1. **列出页面→接口调用清单**：从代码中提取每个页面的全部 HTTP 调用（方法 + URL + 参数 + 期望字段），生成核对表：

```
| 页面 | T5 编号 | 方法与URL | 请求参数 | 页面使用字段 | 返回字段核对 | 错误码处理 |
```

2. **逐项核对 T5 清单**，每项给出 ✅/❌：
   - URL 与方法是否与 T5 一致（前后缀、路径参数）
   - 请求参数名是否一致（如 `pageSize` 而非 `page_size`、`auditReason` 而非 `reason`）
   - 页面读取的返回字段是否在 T5 `data` 示例中（如 M-009 list 项的 `userName`）
   - 错误码是否有对应提示文案（对照 `03-错误码分段表.md`）
3. **差异处置**：
   - 页面错 → 修复页面；
   - 契约未覆盖 → 写入 `docs/progress/backlog.md` 并反馈 A（附页面与期望）；
   - 契约已含但后端未实现 → 反馈 A，标注阻塞。
4. **输出核对表**到 `docs/phase4/member-c/deliverables/`，作为 C6 交付物之一。

## 重点核对项（易错清单）

| 易错点 | 说明 |
|---|---|
| 分页参数 | T5 约定 `page/pageSize`，返回 `{list, total}` |
| 统一返回 | 所有接口返回 `{code, message, data, total}`，code 200 为成功 |
| 金额字段 | `price/originalPrice/payAmount/refundAmount`，前端展示两位小数 |
| 状态值 | 全部为枚举字符串（如 `ON_SALE`/`PAID`/`REFUNDING`），禁止数字码 |
| 审核原因 | `auditReason`（驳回必填）；售后回复 `reply`；裁决 `adminResult` + `agree` 布尔 |
| 发货 | M-011 参数 `shippingNo` 必填 |
| 脱敏字段 | M-009 list 含 `userName`（脱敏），页面直接展示不二次加工 |

## 完成自查

- [ ] 34 个接口（M-001~015 / A-001~019）全部有页面调用或明确标注"暂不调用"
- [ ] 核对表无 ❌ 遗留（或差异已走 backlog 并反馈 A）
- [ ] 核对表已提交到 deliverables/ 并随进度文件一起推送
